#!/bin/sh
exec java -jar app.jar --server.port="${FUNCTIONS_CUSTOMHANDLER_PORT:-8080}"
