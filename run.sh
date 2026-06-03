#!/bin/bash
set -e

cd /Users/fatan/workspace/springclaw

echo "=========================================="
echo "  SpringClaw — Building & Running"
echo "=========================================="

# Step 1: Check Java
echo ""
echo "[1/3] Checking Java..."
java -version 2>&1 || {
    echo "ERROR: Java 21+ is required but not installed."
    echo "Install with: brew install openjdk@21"
    exit 1
}
echo "Java is available!"

# Step 2: Build
echo ""
echo "[2/3] Building project..."
./gradlew build --no-daemon -q

echo "Build successful!"

# Step 3: Run
echo ""
echo "[3/3] Starting SpringClaw sample-basic..."
echo "=========================================="
echo ""

./gradlew :springclaw-samples:sample-basic:bootRun --no-daemon
