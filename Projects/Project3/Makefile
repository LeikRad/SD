FIND := $(shell which gfind 2>/dev/null || which find)
JAVA  := $(shell $(FIND) src -type f -name *.java)
CLASS := $(patsubst src/%.java, build/%.class, $(JAVA))

JAVACOPT := -Xlint:deprecation

all: compile build/output.jar

build/.create:
	@mkdir -p build
	@touch $@

compile: build/.create
	@echo "[JAVAC] Compiling all sources"
	@javac $(JAVACOPT) $(JAVA) -d build


build/MANIFEST.MF:
	@echo "[CREATE] MANIFEST.MF"
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class: sd.main.Main" >> $@

build/output.jar: $(CLASS) build/MANIFEST.MF
	@echo "[JAR] $@"
	@jar cfm $@ build/MANIFEST.MF -C build .

run: build/output.jar
	@java -jar $<

# RMI-specific targets
rmi-server: build/output.jar
	@echo "[RMI] Starting Election Server"
	@java -cp build sd.main.NewRMI.RMIElectionServer

rmi-clerk: build/output.jar
	@echo "[RMI] Starting Clerk Client"
	@java -cp build sd.main.clientSide.entities.RMIClerkClient

rmi-voter: build/output.jar
	@echo "[RMI] Starting Voter Client"
	@java -cp build sd.main.clientSide.entities.RMIVoterClient

rmi-pollster: build/output.jar
	@echo "[RMI] Starting Pollster Client"
	@java -cp build sd.main.clientSide.entities.RMIPollsterClient

# Start multiple clients at once
rmi-all-clients: build/output.jar
	@echo "[RMI] Starting one of each client type"
	@java -cp build sd.main.clientSide.entities.RMIClerkClient &
	@sleep 1
	@java -cp build sd.main.clientSide.entities.RMIPollsterClient &
	@sleep 1
	@java -cp build sd.main.clientSide.entities.RMIVoterClient

rmi-all-clients2: build/output.jar
	@echo "[RMI] Starting clients (use Ctrl+C to exit)"
	@xterm -title "RMI Clerk" -e "java -cp build sd.main.clientSide.entities.RMIClerkClient" &
	@xterm -title "RMI Pollster" -e "java -cp build sd.main.clientSide.entities.RMIPollsterClient" &
	@xterm -title "RMI Voter" -e "java -cp build sd.main.clientSide.entities.RMIVoterClient"

# Run RMI registry separately if needed
rmi-registry:
	@echo "[RMI] Starting RMI registry on port 1099"
	@rmiregistry 1099 &

gui: build/output.jar
	@echo "[GUI] Starting RMI Election System Control Panel"
	@java -cp build sd.main.GUI

.PHONY: clean rmi-server rmi-clerk rmi-voter rmi-pollster rmi-all-clients rmi-registry
clean:
	@echo "[RM] build"
	@rm -rf build
	@echo "[RM] output.jar"
	@rm -rf build/output.jar