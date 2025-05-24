package horizon.http;

import horizon.core.protocol.ProtocolAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpProtocolTest {

    @Test
    void shouldReturnCorrectName() {
        // Given
        HttpProtocol protocol = new HttpProtocol();
        
        // When
        String name = protocol.getName();
        
        // Then
        assertThat(name).isEqualTo("HTTP");
    }
    
    @Test
    void shouldCreateHttpProtocolAdapter() {
        // Given
        HttpProtocol protocol = new HttpProtocol();
        
        // When
        ProtocolAdapter<FullHttpRequest, FullHttpResponse> adapter = protocol.createAdapter();
        
        // Then
        assertThat(adapter).isNotNull();
        assertThat(adapter).isInstanceOf(HttpProtocolAdapter.class);
    }
}