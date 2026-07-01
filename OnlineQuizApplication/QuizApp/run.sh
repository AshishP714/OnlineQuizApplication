#!/bin/bash
# Runs the Online Quiz Application. Builds first if the "out" directory is missing.
# Usage: ./run.sh
set -e

OUT_DIR="out"
LIB_DIR="lib"

if [ ! -d "$OUT_DIR" ]; then
    echo "No build found, building first..."
    ./build.sh
fi

mkdir -p data

CP="$OUT_DIR:$LIB_DIR/sqlite-jdbc-3.44.1.0.jar:$LIB_DIR/slf4j-api-1.7.32.jar:$LIB_DIR/slf4j-nop-1.7.32.jar"

java -cp "$CP" com.quizapp.Main
