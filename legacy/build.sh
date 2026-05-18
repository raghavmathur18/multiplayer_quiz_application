#!/bin/bash
# ============================================================
# QuizArena Build Script (no Maven needed)
# Requires: Java 17+, and the JARs in the lib/ folder
# ============================================================

echo "=== QuizArena Build Script ==="

# Check Java
if ! command -v java &>/dev/null; then
  echo "ERROR: Java not found. Install Java 17+ first."
  exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -1)
echo "Java: $JAVA_VER"

# Check lib jars
MYSQL_JAR="lib/mysql-connector-java-8.0.33.jar"
JFREE_JAR="lib/jfreechart-1.5.4.jar"

if [ ! -f "$MYSQL_JAR" ]; then
  echo ""
  echo "ERROR: $MYSQL_JAR not found."
  echo "Download from: https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar"
  echo "Place it in: QuizApp/lib/"
  exit 1
fi
if [ ! -f "$JFREE_JAR" ]; then
  echo ""
  echo "ERROR: $JFREE_JAR not found."
  echo "Download from: https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.4/jfreechart-1.5.4.jar"
  echo "Place it in: QuizApp/lib/"
  exit 1
fi

CLASSPATH="$MYSQL_JAR:$JFREE_JAR"
SRC_DIR="src/main/java"
OUT_DIR="target/classes"
JAR_OUT="target/QuizArena.jar"

# Compile
echo ""
echo "[1/3] Compiling Java sources..."
mkdir -p "$OUT_DIR"

find "$SRC_DIR" -name "*.java" > /tmp/sources.txt
javac -cp "$CLASSPATH" -d "$OUT_DIR" --source-path "$SRC_DIR" @/tmp/sources.txt

if [ $? -ne 0 ]; then
  echo "COMPILATION FAILED"
  exit 1
fi
echo "      Compilation successful!"

# Package
echo "[2/3] Packaging JAR..."
mkdir -p target
cp "$MYSQL_JAR" "$OUT_DIR/"
cp "$JFREE_JAR" "$OUT_DIR/"

# Create manifest
cat > /tmp/MANIFEST.MF << 'EOF'
Main-Class: com.quizapp.Main
Class-Path: lib/mysql-connector-java-8.0.33.jar lib/jfreechart-1.5.4.jar
EOF

jar cfm "$JAR_OUT" /tmp/MANIFEST.MF -C "$OUT_DIR" .
echo "      JAR created: $JAR_OUT"

echo "[3/3] Done!"
echo ""
echo "=============================="
echo " To run the Socket SERVER:"
echo "   java -cp $JAR_OUT:lib/* com.quizapp.Main server"
echo ""
echo " To run the GUI CLIENT:"
echo "   java -cp $JAR_OUT:lib/* com.quizapp.Main"
echo "=============================="
