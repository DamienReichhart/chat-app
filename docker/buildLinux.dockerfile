FROM ubuntu:24.04

WORKDIR /app

RUN apt-get update && \
    apt-get upgrade -y

RUN apt-get install -y \
    make \
    openjdk-21-jdk openjdk-21-jre \
    openjfx \
    maven \
    wget \
    unzip \
    fakeroot \
    rpm \
    binutils

# Set JAVA_HOME environment variable
# Set PATH to include the JAVA_HOME/bin directory
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="${JAVA_HOME}/bin:${PATH}"


