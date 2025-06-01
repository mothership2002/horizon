package horizon.web.grpc;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for gRPC server in Horizon Framework.
 * Allows customization of gRPC-specific settings.
 */
public class GrpcConfiguration {

    private int maxInboundMessageSize = 4 * 1024 * 1024; // 4MB default
    private int maxInboundMetadataSize = 8192; // 8KB default
    private boolean enableCompression = true;
    private final List<ServerInterceptor> interceptors = new ArrayList<>();
    private final List<BindableService> services = new ArrayList<>();

    // TLS/SSL configuration
    private boolean enableTls = false;
    private File certChainFile = null;
    private File privateKeyFile = null;

    /**
     * Gets the maximum inbound message size in bytes.
     */
    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    /**
     * Sets the maximum inbound message size in bytes.
     */
    public GrpcConfiguration setMaxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
        return this;
    }

    /**
     * Gets the maximum inbound metadata size in bytes.
     */
    public int getMaxInboundMetadataSize() {
        return maxInboundMetadataSize;
    }

    /**
     * Sets the maximum inbound metadata size in bytes.
     */
    public GrpcConfiguration setMaxInboundMetadataSize(int maxInboundMetadataSize) {
        this.maxInboundMetadataSize = maxInboundMetadataSize;
        return this;
    }

    /**
     * Checks if compression is enabled.
     */
    public boolean isCompressionEnabled() {
        return enableCompression;
    }

    /**
     * Enables or disables compression.
     */
    public GrpcConfiguration setCompressionEnabled(boolean enableCompression) {
        this.enableCompression = enableCompression;
        return this;
    }

    /**
     * Adds a server interceptor.
     */
    public GrpcConfiguration addInterceptor(ServerInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    /**
     * Gets all registered interceptors.
     */
    public List<ServerInterceptor> getInterceptors() {
        return new ArrayList<>(interceptors);
    }

    /**
     * Adds a bindable service.
     * This allows registration of standard gRPC services alongside Horizon conductors.
     */
    public GrpcConfiguration addService(BindableService service) {
        this.services.add(service);
        return this;
    }

    /**
     * Gets all registered services.
     */
    public List<BindableService> getServices() {
        return new ArrayList<>(services);
    }

    /**
     * Checks if TLS is enabled.
     */
    public boolean isTlsEnabled() {
        return enableTls;
    }

    /**
     * Gets the certificate chain file.
     */
    public File getCertChainFile() {
        return certChainFile;
    }

    /**
     * Gets the private key file.
     */
    public File getPrivateKeyFile() {
        return privateKeyFile;
    }

    /**
     * Enables TLS with the specified certificate chain and private key files.
     * 
     * @param certChainFile The certificate chain file
     * @param privateKeyFile The private key file
     * @return This configuration instance for method chaining
     */
    public GrpcConfiguration enableTls(File certChainFile, File privateKeyFile) {
        this.enableTls = true;
        this.certChainFile = certChainFile;
        this.privateKeyFile = privateKeyFile;
        return this;
    }

    /**
     * Disables TLS.
     * 
     * @return This configuration instance for method chaining
     */
    public GrpcConfiguration disableTls() {
        this.enableTls = false;
        this.certChainFile = null;
        this.privateKeyFile = null;
        return this;
    }

    /**
     * Creates a default configuration.
     */
    public static GrpcConfiguration defaultConfig() {
        return new GrpcConfiguration();
    }
}
