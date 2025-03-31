package horizon.core.model.output.http.netty;

import horizon.core.model.output.http.HttpRawOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NettyHttpRawOutput extends HttpRawOutput {

    public NettyHttpRawOutput() {
    }

    public NettyHttpRawOutput(int statusCode, Map<String, String> headers, String body) {
        super(statusCode, headers, body);
    }
    public FullHttpResponse toNettyResponse() {

        ByteBuf content = Unpooled.copiedBuffer(getBody() != null ? getBody() : "", StandardCharsets.UTF_8);
        HttpResponseStatus status = HttpResponseStatus.valueOf(getStatusCode());
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                content
        );

        for (Map.Entry<String, String> header : getHeaders().entrySet()) {
            response.headers().set(header.getKey(), header.getValue());
        }

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }

    public static NettyHttpRawOutput create(int statusCode, Map<String, String> headers, String body) {
        NettyHttpRawOutput output = new NettyHttpRawOutput();
        output.setStatusCode(statusCode);
        output.setHeaders(headers);
        output.setBody(body);
        return output;
    }

    public static NettyHttpRawOutput createJsonResponse(String jsonBody) {
        NettyHttpRawOutput output = new NettyHttpRawOutput();
        output.setStatusCode(HttpResponseStatus.OK.code());
        output.addHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json");
        output.setBody(jsonBody);
        return output;
    }

}
