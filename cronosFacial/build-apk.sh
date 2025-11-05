#!/bin/bash

# Set JAVA_HOME to valid Java 17 installation
export JAVA_HOME=/usr/local/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home

echo "Using JAVA_HOME: $JAVA_HOME"
echo "Java version:"
$JAVA_HOME/bin/java -version

# Build debug APK
echo ""
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ Build failed. Check the error messages above."
    exit 1
fi
