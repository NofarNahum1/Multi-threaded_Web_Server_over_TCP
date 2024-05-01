#!/bin/bash
SOURCE_DIR="src/"
# Set the name of your Java source file (replace ServerMain.java with your actual filename)
JAVA_SOURCE_FILE="src/Main.java src/MultiThreadedServer.java src/HTTPStatusCode.java src/HTTPRequest.java src/ClientHandler.java" 

# Set the name of your Java class (replace ServerMain with your actual class name)
JAVA_CLASS_NAME="Main"

# Compile the Java source code
javac $JAVA_SOURCE_FILE