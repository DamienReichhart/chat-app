# Define variables for Maven, JavaFX goals, and JPackage options
MVN = mvn
JAVA_FX_GOAL = javafx:run
SPRING_GOAL = spring-boot:run
APP_NAME = AP4

# Define jpackage variables
JPACKAGE = jpackage
JPACKAGE_CMD = $(JPACKAGE)
ICON_PATH = src/main/resources/logo.ico
INPUT_DIR = target
JVM_OPTIONS = --java-options "-Xmx2048m"
JAR_FILE_CLIENT = chat-client-1.0-SNAPSHOT.jar
MAIN_JAR_CLIENT = $(JAR_FILE_CLIENT)
MAIN_CLASS_CLIENT = com.ap4.client.Bootstrap
OUTPUT_DIR = build
VENDOR = "REICHHART Damien"

# Function to run jpackage for different installer types
define run_jpackage
	$(JPACKAGE_CMD) --name $(APP_NAME) \
		--input $(INPUT_DIR) \
		--main-jar $(MAIN_JAR_CLIENT) \
		--main-class $(MAIN_CLASS_CLIENT) \
		--type $(1) \
		--dest $(OUTPUT_DIR) \
		$(JVM_OPTIONS) \
		--icon $(ICON_PATH) \
		--vendor $(VENDOR)
endef

# Full .PHONY declaration for all targets
.PHONY: help maven-clean maven-compile maven-package maven-install \
        maven-clean-common maven-compile-common maven-package-common maven-install-common \
        maven-run-client maven-clean-client maven-compile-client maven-package-client maven-install-client \
        jpackage-windows-client jpackage-linux-client \
        maven-run-server maven-clean-server maven-compile-server maven-install-server

####################################
# Main project targets
####################################

# Help target to display available commands
help:
	@echo "Available targets:"
	@echo "  maven-clean              - Clean the entire project"
	@echo "  maven-compile            - Compile the entire project"
	@echo "  maven-package            - Package the entire project"
	@echo "  maven-install            - Install the entire project"
	@echo "  maven-clean-common       - Clean the common module"
	@echo "  maven-compile-common     - Compile the common module"
	@echo "  maven-package-common     - Package the common module"
	@echo "  maven-install-common     - Install the common module"
	@echo "  maven-run-client         - Run the client module with JavaFX"
	@echo "  maven-clean-client       - Clean the client module"
	@echo "  maven-compile-client     - Compile the client module"
	@echo "  maven-package-client     - Package the client module"
	@echo "  maven-install-client     - Install the client module"
	@echo "  jpackage-windows-client  - Create Windows installers for the client"
	@echo "  jpackage-linux-client    - Create Linux installers for the client"
	@echo "  maven-run-server         - Run the server module with Spring Boot"
	@echo "  maven-clean-server       - Clean the server module"
	@echo "  maven-compile-server     - Compile the server module"
	@echo "  maven-install-server     - Install the server module"

# Target to clean the project
maven-clean:
	$(MVN) clean

# Target to compile the project
maven-compile:
	$(MVN) compile

# Target to package the project using Maven
maven-package:
	$(MVN) package

# Target to install the project
maven-install:
	$(MVN) install

####################################
# Common module targets (chat-common)
####################################

maven-clean-common:
	cd chat-common && $(MVN) clean

maven-compile-common:
	cd chat-common && $(MVN) compile

maven-package-common:
	cd chat-common && $(MVN) package

maven-install-common:
	cd chat-common && $(MVN) install

####################################
# Client module targets (chat-client)
####################################

# Target to run the client (executes clean, compile, package, install then runs JavaFX)
maven-run-client:
	cd chat-client && $(MVN) clean compile package install $(JAVA_FX_GOAL)

# Target to clean the client project
maven-clean-client:
	cd chat-client && $(MVN) clean

# Target to compile the client project
maven-compile-client:
	cd chat-client && $(MVN) compile

# Target to package the client project
maven-package-client:
	cd chat-client && $(MVN) package

# Target to install the client project
maven-install-client:
	cd chat-client && $(MVN) install

# Target to create native installers for Windows for the client
jpackage-windows-client:
	$(MVN) clean compile package install
	cd chat-client && $(call run_jpackage,exe)
	cd chat-client && $(call run_jpackage,msi)

# Target to create native installers for Linux for the client
jpackage-linux-client:
	$(MVN) clean compile package install
	cd chat-client && $(call run_jpackage,deb)
	cd chat-client && $(call run_jpackage,rpm)

####################################
# Server module targets (chat-server)
####################################

# Target to run the server (executes clean, compile, package, install then runs Spring Boot)
maven-run-server:
	cd chat-server && $(MVN) clean compile package install $(SPRING_GOAL)

# Target to clean the server project
maven-clean-server:
	cd chat-server && $(MVN) clean

# Target to compile the server project
maven-compile-server:
	cd chat-server && $(MVN) compile

# Target to install the server project
maven-install-server:
	cd chat-server && $(MVN) install
