#!/bin/bash
# Verify fx:id and onAction references in FXML against controller fields/methods
cd /home/personnadev/Documents/mentoruadb/src/main/resources/com/uadb/mentoruadb/fxml || exit 1
TOTAL_ISSUES=0
for fxml in *.fxml; do
  ctrl_short=$(grep -o 'fx:controller="com.uadb.mentoruadb.controller.[A-Za-z]*"' "$fxml" | sed 's/.*controller\.//;s/"//')
  ctrl_java="/home/personnadev/Documents/mentoruadb/src/main/java/com/uadb/mentoruadb/controller/${ctrl_short}.java"
  if [ ! -f "$ctrl_java" ]; then
    echo "[$fxml] MISSING CONTROLLER FILE: $ctrl_java"
    TOTAL_ISSUES=$((TOTAL_ISSUES+1))
    continue
  fi
  ids=$(grep -o 'fx:id="[A-Za-z0-9_]*"' "$fxml" | sed 's/fx:id="//;s/"//' | sort -u)
  for id in $ids; do
    if ! grep -qE "(private|public|protected)[^=;()]*[[:space:]]${id}[[:space:]]*(;|=)" "$ctrl_java"; then
      echo "[$fxml] fx:id '$id' NOT FOUND in $ctrl_short.java"
      TOTAL_ISSUES=$((TOTAL_ISSUES+1))
    fi
  done
  actions=$(grep -o 'onAction="#[A-Za-z0-9_]*"' "$fxml" | sed 's/onAction="#//;s/"//' | sort -u)
  for act in $actions; do
    if ! grep -qE "(private|public|protected)[^;]*[[:space:]]${act}[[:space:]]*\(" "$ctrl_java"; then
      echo "[$fxml] onAction '#${act}' NOT FOUND in $ctrl_short.java"
      TOTAL_ISSUES=$((TOTAL_ISSUES+1))
    fi
  done
done
echo "----"
echo "TOTAL BINDING ISSUES: $TOTAL_ISSUES"
