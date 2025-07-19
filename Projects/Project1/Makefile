FIND := $(shell which gfind 2>/dev/null || which find)
JAVA  := $(shell $(FIND) src -type f -name *.java)

JAVACOPT := -Xlint:deprecation

all: build/.create build/output.jar

build/.create:
	@mkdir -p build

build/output.jar: $(JAVA) build/MANIFEST.MF
	@echo "[JAVAC] Compiling Java sources..."
	@javac $(JAVACOPT) -d build $(JAVA)
	@echo "[JAR] Creating output.jar"
	@jar cfm build/output.jar build/MANIFEST.MF -C build .

build/MANIFEST.MF:
	@echo "[CREATE] MANIFEST.MF"
	@echo "Manifest-Version: 1.0" > build/MANIFEST.MF
	@echo "Main-Class: sd.main.Main" >> build/MANIFEST.MF

run: build/output.jar
	@java -jar build/output.jar

.PHONY: clean
clean:
	@echo "[RM] build"
	@rm -rf build