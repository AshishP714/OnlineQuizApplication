#!/bin/bash
# Builds the Online Quiz Application.
# Usage: ./build.sh
set -e

SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="lib"

CP="$LIB_DIR/sqlite-jdbc-3.44.1.0.jar:$LIB_DIR/slf4j-api-1.7.32.jar"

echo "Cleaning previous build..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

echo "Compiling Java sources..."
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$CP" -d "$OUT_DIR" @sources.txt

echo "Build complete. Class files are in $OUT_DIR/"
echo "Run the application with: ./run.sh"
