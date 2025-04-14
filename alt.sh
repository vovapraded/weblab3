#!/bin/bash

set -e

REPLACEMENTS_FILE="./alt-replacements.properties"
ORIGINAL_SRC="./src/main/java"
ALT_SRC="./target/alt-src"

echo "[alt.sh] Начало работы. Текущая директория: $(pwd)"

# Проверка наличия файла замен
if [ ! -f "$REPLACEMENTS_FILE" ]; then
  echo "[alt.sh] ❌ Файл замен не найден: $REPLACEMENTS_FILE"
  exit 1
fi

# Копирование исходников
echo "[alt.sh] 🗂 Копируем исходники..."
rm -rf "$ALT_SRC"
mkdir -p "$ALT_SRC"
cp -r "$ORIGINAL_SRC"/* "$ALT_SRC"

echo "[alt.sh] 🔁 Заменяем содержимое файлов:"
while IFS='=' read -r raw_search raw_replace; do
  # Удаляем пробелы и \r
  search=$(echo "$raw_search" | tr -d '\r' | xargs)
  replace=$(echo "$raw_replace" | tr -d '\r' | xargs)

  # Пропускаем пустые и закомментированные строки
  if [[ -z "$search" || "$search" == \#* ]]; then
    continue
  fi

  echo "  ✏️ '$search' → '$replace'"
  find "$ALT_SRC" -type f -name "*.java" -exec perl -pi -e "s/\Q$search\E/$replace/g" {} +
done < "$REPLACEMENTS_FILE"

echo "[alt.sh] 📝 Переименовываем файлы:"
while IFS='=' read -r raw_search raw_replace; do
  search=$(echo "$raw_search" | tr -d '\r' | xargs)
  replace=$(echo "$raw_replace" | tr -d '\r' | xargs)

  if [[ -z "$search" || "$search" == \#* ]]; then
    continue
  fi

  # Поиск и переименование файлов, в именах которых есть строка поиска
  find "$ALT_SRC" -type f -name "*$search*.java" | while read -r file; do
    newfile=$(echo "$file" | sed "s/$search/$replace/g")
    if [[ "$file" != "$newfile" ]]; then
      echo "  📄 $file → $newfile"
      mv "$file" "$newfile"
    fi
  done
done < "$REPLACEMENTS_FILE"

echo "[alt.sh] ✅ Готово! Проверка:"
grep -r -n "Validator3" "$ALT_SRC" || echo "✔️ 'Validator3' не найден — всё заменено"
