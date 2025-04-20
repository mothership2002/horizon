package horizon.http.netty;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.protocol.ProtocolAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Objects;

/**
 * Implementation of the ProtocolAdapter interface for Netty HTTP.
 * This class handles the conversion between Netty HTTP requests/responses and Horizon's RawInput/RawOutput.
 *
 * @param <I> the type of raw input this adapter produces
 * @param <O> the type of raw output this adapter consumes
 */
public class NettyHttpAdapter<I extends RawInput, O extends RawOutput> 
        implements ProtocolAdapter<I, O, FullHttpRequest, FullHttpResponse> {

    private final NettyInputConverter<I> inputConverter;

    /**
     * Creates a new NettyHttpAdapter with the specified input converter.
     *
     * @param inputConverter the converter to convert Netty HTTP requests to raw input
     * @throws NullPointerException if inputConverter is null
     */
    public NettyHttpAdapter(NettyInputConverter<I> inputConverter) {
        this.inputConverter = Objects.requireNonNull(inputConverter, "inputConverter must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public I convertToInput(FullHttpRequest request, String remoteAddress) 
            throws IllegalArgumentException, NullPointerException {
        return inputConverter.convert(request, remoteAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FullHttpResponse convertToResponse(O output, Object context) 
            throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(output, "output must not be null");

        // The context is expected to be a Boolean representing keepAlive
        boolean keepAlive = (context instanceof Boolean) ? (Boolean) context : false;

        // Get the content from the output
        Object rawContent = output.getContent();
        String content = rawContent != null ? rawContent.toString() : "";
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);

        // Get the status code from the output or default to OK
        HttpResponseStatus status = HttpResponseStatus.valueOf(output.getStatusCode());

        // Create the HTTP response
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, buffer);

        // Set headers
        String contentType = "text/plain; charset=UTF-8";
        if (output.getMetadata() != null && output.getMetadata() instanceof String) {
            contentType = (String) output.getMetadata();
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FullHttpResponse createErrorResponse(Throwable e, Object context) {
        // The context is expected to be a Boolean representing keepAlive
        boolean keepAlive = (context instanceof Boolean) ? (Boolean) context : false;

        // Create the error message
        String content = "Error: " + e.getMessage();
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);

        // Create the HTTP response
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buffer);

        // Set headers
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FullHttpResponse createForbiddenResponse(Object context) {
        // The context is expected to be a Boolean representing keepAlive
        boolean keepAlive = (context instanceof Boolean) ? (Boolean) context : false;

        // Create the forbidden message
        String content = "Forbidden";
        ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);

        // Create the HTTP response
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, buffer);

        // Set headers
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        return response;
    }
}
