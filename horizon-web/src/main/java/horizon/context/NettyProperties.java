package horizon.context;

import horizon.core.context.Properties;

public class NettyProperties implements Properties {

    private final int eventLoopGroupSize;
    private final int workerThreadSize;
    private final int port;
    private final int maxContentLength;
    private final int readTimeoutMillis;
    private final int allIdleTimeMillis;
    private final int writeTimeoutMillis;
    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;

    private NettyProperties(Builder builder) {
        this.eventLoopGroupSize = builder.eventLoopGroupSize;
        this.workerThreadSize = builder.workerThreadSize;
        this.port = builder.port;
        this.maxContentLength = builder.maxContentLength;
        this.readTimeoutMillis = builder.readTimeoutMillis;
        this.allIdleTimeMillis = builder.allIdleTimeMillis;
        this.writeTimeoutMillis = builder.writeTimeoutMillis;
        this.maxInitialLineLength = builder.maxInitialLineLength;
        this.maxHeaderSize = builder.maxHeaderSize;
        this.maxChunkSize = builder.maxChunkSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int eventLoopGroupSize = 1;
        private int workerThreadSize = 0;
        private int port = 8080;
        private int maxContentLength = 4 * 1024 * 1024;
        private int readTimeoutMillis = 10000;
        private int allIdleTimeMillis = 10000;
        private int writeTimeoutMillis = 10000;
        private int maxInitialLineLength = 8192;
        private int maxHeaderSize = 8192;
        private int maxChunkSize = 4096;

        public Builder eventLoopGroupSize(int value) {
            this.eventLoopGroupSize = value;
            return this;
        }

        public Builder workerThreadSize(int value) {
            this.workerThreadSize = value;
            return this;
        }

        public Builder port(int value) {
            this.port = value;
            return this;
        }

        public Builder maxContentLength(int value) {
            this.maxContentLength = value;
            return this;
        }

        public Builder readTimeoutMillis(int value) {
            this.readTimeoutMillis = value;
            return this;
        }

        public Builder allIdleTimeMillis(int value) {
            this.allIdleTimeMillis = value;
            return this;
        }

        public Builder writeTimeoutMillis(int value) {
            this.writeTimeoutMillis = value;
            return this;
        }

        public Builder maxInitialLineLength(int value) {
            this.maxInitialLineLength = value;
            return this;
        }

        public Builder maxHeaderSize(int value) {
            this.maxHeaderSize = value;
            return this;
        }

        public Builder maxChunkSize(int value) {
            this.maxChunkSize = value;
            return this;
        }

        public NettyProperties build() {
            return new NettyProperties(this);
        }
    }

    public int getEventLoopGroupSize() {
        return eventLoopGroupSize;
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    public int getPort() {
        return port;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public int getAllIdleTimeMillis() {
        return allIdleTimeMillis;
    }

    public int getWriteTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public int getMaxInitialLineLength() {
        return maxInitialLineLength;
    }

    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }
}
