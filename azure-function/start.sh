#!/bin/sh
# Sobe o Spring Boot como custom handler. Tudo que sai aqui vai para o log do Function App
# e tambem para /home/LogFiles/start.log (legivel pelo Kudu), caso o log stream nao mostre.
LOG=/home/LogFiles/start.log
log() {
  echo "[start.sh] $*"
  echo "[start.sh] $*" >> "$LOG" 2>/dev/null
}

PORT_HANDLER="${FUNCTIONS_CUSTOMHANDLER_PORT:-8080}"

# Prefere o Java embutido no pacote (./jre); se nao existir, tenta o do sistema.
JAVA="./jre/bin/java"
if [ -f "$JAVA" ]; then
  chmod +x ./jre/bin/* ./jre/lib/jspawnhelper 2>/dev/null
fi
if [ ! -x "$JAVA" ]; then
  JAVA="$(command -v java || echo java)"
fi

log "inicio $(date -u +%FT%TZ) pwd=$(pwd) usuario=$(id -un 2>/dev/null)"
log "java=$JAVA porta=$PORT_HANDLER"
"$JAVA" -version 2>&1 | while read -r line; do log "$line"; done
ls -la | while read -r line; do log "$line"; done

exec "$JAVA" -jar app.jar --server.port="$PORT_HANDLER"
