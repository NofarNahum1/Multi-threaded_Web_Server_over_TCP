Multi-Threaded Web Server Documentation - Overview
This document describes the implementation of a multi-threaded web server that listens on TCP port 8080 and serves HTML pages, images, and other files from a specified root directory. 
The server supports basic HTTP methods such as GET and POST, along with HEAD and TRACE, and handles various content types with appropriate response codes.

Configuration:
The server's configurations are specified in config.ini, including the listening port (8080), root directory (~/www/lab/html/), default page (index.html), and maximum number of threads (10).

Classes:
Main: The entry point of the server. It initializes the server with settings from config.ini and starts listening for incoming connections.
MultiThreadedServer: Sets up the server socket and manages thread pool for handling client connections. It ensures the server does not exceed the maximum allowed threads and gracefully handles server shutdown.
ClientHandler: Processes individual client requests in separate threads. It serves requested files, handles supported HTTP methods, and ensures clients cannot access files outside the root directory.
HTTPRequest: Parses the HTTP request, extracting important details such as method type, requested resource, and parameters for both GET and POST requests.
HTTPStatusCode: Manages HTTP response codes and constructs response headers. It supports sending both standard and chunked responses.
Design and Implementation
The server is designed with a focus on extensibility, robustness, and compliance with basic HTTP protocol standards. 
We use a fixed thread pool to limit the number of concurrent threads, preventing resource exhaustion. The ClientHandler class is central to processing requests, ensuring that each client's interaction is isolated and handled efficiently. 
The server can respond with different content types and supports basic file serving, along with chunked transfer encoding based on client request headers.

Security measures include preventing directory traversal attacks by sanitizing paths and ensuring that clients cannot access files outside the predefined root directory. 
The server also gracefully handles exceptions, providing informative error messages to both the console and the client, ensuring continuous operation even in the face of errors.

**Note**: 
After making a request in the command line, the user needs to press Enter twice to receive a response.

