FROM mcr.microsoft.com/windows/servercore:ltsc2019

WORKDIR C:/app

SHELL ["powershell", "-Command", "$ErrorActionPreference = 'Stop'; $ProgressPreference = 'SilentlyContinue';"]

# Install Chocolatey
RUN Set-ExecutionPolicy Bypass -Scope Process -Force; \
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; \
    iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# Install required tools
RUN choco install -y openjdk21 maven make

# Set environment variables
RUN setx JAVA_HOME "C:\Program Files\OpenJDK\openjdk-21" /M; \
    setx PATH "$env:PATH;C:\Program Files\OpenJDK\openjdk-21\bin" /M 