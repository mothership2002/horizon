package horizon.core.rendezvous.netty;

import horizon.core.model.RawInput;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Converter interface for converting Netty HTTP requests to Horizon raw input.
 *
 * @param <I> the type of raw input this converter produces
 */
public interface NettyInputConverter<I extends RawInput> {
    
    /**
     * Converts a Netty HTTP request to a Horizon raw input.
     *
     * @param request the Netty HTTP request to convert
     * @param remoteAddress the remote address of the client
     * @return the converted raw input
     * @throws IllegalArgumentException if the request cannot be converted
     * @throws NullPointerException if request is null
     */
    I convert(FullHttpRequest request, String remoteAddress) throws IllegalArgumentException, NullPointerException;
}