#!/bin/bash
set -e

JAVAFX_HOME="${JAVAFX_HOME:-$HOME/JavaFX/javafx-sdk-25.0.1}"

if [ ! -d "$JAVAFX_HOME/lib" ]; then
  echo "Erreur : SDK JavaFX introuvable dans $JAVAFX_HOME"
  echo "Définir JAVAFX_HOME manuellement : export JAVAFX_HOME=/chemin/vers/javafx-sdk"
  exit 1
fi

echo "Build Maven..."
mvn clean package -DskipTests -q

echo "Copie des librairies natives JavaFX..."
cp "$JAVAFX_HOME"/lib/*.dylib target/jpackage-input/

echo "Lancement de jpackage..."
jpackage \
  --type dmg \
  --name Ambre \
  --app-version 1.0.0 \
  --input target/jpackage-input \
  --main-jar ambre-1.0.0.jar \
  --main-class com.ambre.Main \
  --dest target/dist \
  --module-path "$JAVAFX_HOME/lib" \
  --add-modules javafx.controls,javafx.fxml \
  --java-options "-Djava.library.path=\$APPDIR" \
  --mac-package-name Ambre

echo ""
echo "DMG généré : target/dist/Ambre-1.0.0.dmg"
