# Horizon Framework HTTP Demo

This is a simple demo project that demonstrates how to use the HTTP module of the Horizon Framework.

## Overview

The demo sets up a simple HTTP server that responds to different endpoints:

- `/` - Returns a welcome message
- `/echo` - Echoes back the request body
- `/json` - Returns a JSON response

## How to Run

1. Make sure you have Java 11 or later installed
2. Navigate to the project root directory
3. Run the following command:

```bash
./gradlew :demo-http:run
```

This will start the HTTP server on port 8080.

## How It Works

The demo uses the following components from the Horizon Framework:

1. **SimpleHttpInput** - A simple implementation of the RawInput interface for HTTP requests
2. **SimpleHttpOutput** - A simple implementation of the RawOutput interface for HTTP responses
3. **SimpleHttpInputConverter** - Converts Netty HTTP requests to SimpleHttpInput objects
4. **SimpleHttpRendezvous** - Processes SimpleHttpInput objects and produces SimpleHttpOutput objects
5. **NettyHttpAdapter** - Adapts between Netty HTTP and Horizon's RawInput/RawOutput
6. **NettyHttpFoyer** - Sets up and manages the Netty HTTP server

The flow of a request through the system is as follows:

1. A client sends an HTTP request to the server
2. The NettyHttpFoyer receives the request and passes it to the NettyHttpAdapter
3. The NettyHttpAdapter uses the SimpleHttpInputConverter to convert the request to a SimpleHttpInput
4. The SimpleHttpInput is passed to the SimpleHttpRendezvous
5. The SimpleHttpRendezvous processes the input and produces a SimpleHttpOutput
6. The SimpleHttpOutput is passed back to the NettyHttpAdapter
7. The NettyHttpAdapter converts the SimpleHttpOutput to a Netty HTTP response
8. The NettyHttpFoyer sends the response back to the client

## Testing the Demo

You can test the demo using curl or a web browser:

```bash
# Test the welcome endpoint
curl http://localhost:8080/

# Test the echo endpoint
curl -X POST -d "Hello, World!" http://localhost:8080/echo

# Test the JSON endpoint
curl http://localhost:8080/json
```

Or open the following URLs in your web browser:

- http://localhost:8080/
- http://localhost:8080/echo
- http://localhost:8080/json