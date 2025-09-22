#!/bin/bash
echo "Chat Application Runner"
echo "======================"

echo "Building project..."
mvn clean install -DskipTests

echo ""
echo "Starting server..."
cd chat-server
mvn spring-boot:run > /tmp/chat-server.log 2>&1 &
SERVER_PID=$!
cd ..

echo ""
echo "Waiting for server to start..."
sleep 5

echo ""
echo "Starting client..."
cd chat-client
mvn javafx:run

echo ""
echo "Shutting down server..."
kill $SERVER_PID
echo "Done!" 