#!/bin/sh
# Diagnostico: sai no log do Function App (Log stream) antes de o Spring subir.
echo "[start.sh] pwd=$(pwd)" >&2
echo "[start.sh] java=$(command -v java || echo NAO ENCONTRADO)" >&2
java -version >&2 || echo "[start.sh] java -version falhou" >&2
echo "[start.sh] porta do handler=${FUNCTIONS_CUSTOMHANDLER_PORT:-8080}" >&2
ls -la >&2

exec java -jar app.jar --server.port="${FUNCTIONS_CUSTOMHANDLER_PORT:-8080}"
