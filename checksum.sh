#!/bin/bash

BASE_DIR="."
OUTPUT_FILE="target/classes/META-INF/MANIFEST.MF"

mkdir -p "$(dirname "$OUTPUT_FILE")"

# Заголовки
echo "Manifest-Version: 1.0" > "$OUTPUT_FILE"
echo "Built-By: $(whoami)" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

find "$BASE_DIR" -type f ! -path "$OUTPUT_FILE" ! -path "*/target/*" ! -path "*/.git/*" | while read -r FILE; do
    REL_PATH="${FILE#$BASE_DIR/}"

    # Сделаем имя ключа безопасным для MANIFEST: только A-Z, a-z, 0-9, -
    SAFE_KEY=$(echo "$REL_PATH" | sed 's/[^A-Za-z0-9]/-/g')

    # Добавим префикс (на всякий случай, если путь начинался с точки или цифры)
    FINAL_KEY="Checksum-$SAFE_KEY"

    # Вычислим хэши
    MD5_HASH=$(md5sum "$FILE" | awk '{print $1}')
    SHA1_HASH=$(shasum -a 1 "$FILE" | awk '{print $1}')

    # Запишем в валидном формате
    echo "$FINAL_KEY-MD5: $MD5_HASH" >> "$OUTPUT_FILE"
    echo "$FINAL_KEY-SHA1: $SHA1_HASH" >> "$OUTPUT_FILE"
done

echo "✅ Валидный MANIFEST.MF с хэшами записан в $OUTPUT_FILE"
