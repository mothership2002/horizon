package horizon.http.netty;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.Foyer;
import horizon.core.rendezvous.Rendezvous;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Netty channel handler that processes HTTP requests by converting them to raw input
 * and passing them to the rendezvous.
 *
 * @param <I> the type of raw input this handler can handle
 */
public class NettyRequestHandler<I extends RawInput> extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRequestHandler.class);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final Foyer<I> foyer;
    private final Rendezvous<I, ?> rendezvous;
    private final NettyInputConverter<I> inputConverter;

    /**
     * Creates a new NettyRequestHandler with the specified foyer, rendezvous, and input converter.
     *
     * @param foyer the foyer to check if requests should be allowed
     * @param rendezvous the rendezvous to pass requests to
     * @param inputConverter the converter to convert Netty requests to raw input
     * @throws NullPointerException if foyer, rendezvous, or inputConverter is null
     */
    public NettyRequestHandler(Foyer<I> foyer, Rendezvous<I, ?> rendezvous, NettyInputConverter<I> inputConverter) {
        this.foyer = Objects.requireNonNull(foyer, "foyer must not be null");
        this.rendezvous = Objects.requireNonNull(rendezvous, "rendezvous must not be null");
        this.inputConverter = Objects.requireNonNull(inputConverter, "inputConverter must not be null");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // If this is not a keep-alive connection, close it after sending the response
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        
        // Get the client's IP address
        String remoteAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString();
        
        // Process the request asynchronously to avoid blocking the Netty event loop
        CompletableFuture.supplyAsync(() -> {
            try {
                // Convert the HTTP request to raw input
                I input = inputConverter.convert(request, remoteAddress);
                
                // Check if the request should be allowed
                if (!foyer.allow(input)) {
                    LOGGER.warn("Request from " + remoteAddress + " was denied by the foyer");
                    return createForbiddenResponse(keepAlive);
                }
                
                // Process the request through the rendezvous
                HorizonContext context = rendezvous.encounter(input);
                
                // If the context has a failure cause, return an error response
                if (context.getFailureCause() != null) {
                    LOGGER.warn("Error processing request: " + context.getFailureCause().getMessage());
                    return createErrorResponse(context.getFailureCause(), keepAlive);
                }
                
                // Get the output from the context
                RawOutput output = rendezvous.fallAway(context);
                
                // Convert the output to an HTTP response
                return createSuccessResponse(output, keepAlive);
            } catch (Exception e) {
                LOGGER.error("Error handling request: {}", e.getMessage(), e);
                return createErrorResponse(e, keepAlive);
            }
        }, EXECUTOR).thenAccept(response -> {
            // Send the response back to the client
            ctx.writeAndFlush(response);
            
            // If this is not a keep-alive connection, close it after sending the response
            if (!keepAlive) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }).exceptionally(e -> {
            LOGGER.error("Error sending response: {}", e.getMessage(), e);
            ctx.close();
            return null;
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Channel exception: {}", cause.getMessage(), cause);
        ctx.close();
    }

    /**
     * Creates a success response from the given output.
     *
     * @param output the output to convert to a response
     * @param keepAlive whether the connection should be kept alive
     * @return the HTTP response
     */
    private FullHttpResponse createSuccessResponse(RawOutput output, boolean keepAlive) {
        // This is a simplified implementation that assumes the output can be converted to a string
        // In a real implementation, you would need to handle different types of output
        String content = output.toString();
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buffer);
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        
        return response;
    }

    /**
     * Creates an error response from the given exception.
     *
     * @param e the exception that caused the error
     * @param keepAlive whether the connection should be kept alive
     * @return the HTTP response
     */
    private FullHttpResponse createErrorResponse(Throwable e, boolean keepAlive) {
        String content = "Error: " + e.getMessage();
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buffer);
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        
        return response;
    }

    /**
     * Creates a forbidden response.
     *
     * @param keepAlive whether the connection should be kept alive
     * @return the HTTP response
     */
    private FullHttpResponse createForbiddenResponse(boolean keepAlive) {
        String content = "Forbidden";
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, buffer);
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
        
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        
        return response;
    }
}