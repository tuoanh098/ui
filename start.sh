#!/usr/bin/env bash
set -e
echo "Downloading JDK..."
curl -L -o /tmp/openjdk.tar.gz "https://corretto.aws/downloads/latest/amazon-corretto-17-x64-linux-jdk.tar.gz"
mkdir -p /tmp/jre
tar -xzf /tmp/openjdk.tar.gz -C /tmp/jre --strip-components=1
export JAVA_HOME=/tmp/jre
export PATH=$JAVA_HOME/bin:$PATH
echo "Starting app..."
exec java -jar target/backend-0.0.1-SNAPSHOT.jar