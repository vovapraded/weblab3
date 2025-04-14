#!/bin/bash
set -e

# Каталоги
ROOT_DIR=$(pwd)
BUILD_DIR="$ROOT_DIR/target/team"
JARS_DIR="$BUILD_DIR/teamJars"
mkdir -p "$JARS_DIR"

# Получаем последние 4 коммита
COMMITS=($(git rev-list --max-count=4 HEAD))
if [ ${#COMMITS[@]} -lt 4 ]; then
  echo "❌ Нужно минимум 4 коммита"
  exit 1
fi

# Обрабатываем 3 предыдущих коммита
for COMMIT in "${COMMITS[@]:1:3}"; do
  SHORT_HASH=$(echo $COMMIT | cut -c1-7)
  WORKTREE_DIR="$BUILD_DIR/worktree_$SHORT_HASH"

  echo "▶ Добавляем worktree $SHORT_HASH"
  git worktree prune
  rm -rf "$WORKTREE_DIR"
  git worktree add "$WORKTREE_DIR" "$COMMIT"

  echo "⚙️ Сборка $SHORT_HASH"
  (cd "$WORKTREE_DIR" && mvn clean package -DskipTests)

  # Находим JAR и копируем
  BUILT_JAR=$(find "$WORKTREE_DIR/target" -name '*.jar' | grep -vE 'sources|javadoc' | head -n 1)
  if [ -f "$BUILT_JAR" ]; then
    cp "$BUILT_JAR" "$JARS_DIR/$(basename "$BUILT_JAR" .jar)-$SHORT_HASH.jar"
    echo "✅ JAR сохранён: $(basename "$BUILT_JAR" .jar)-$SHORT_HASH.jar"
  else
    echo "❌ JAR не найден для $SHORT_HASH"
  fi

  echo "🧹 Удаляем worktree $SHORT_HASH"
  git worktree remove --force "$WORKTREE_DIR"
done

echo "📦 Упаковка ZIP..."
cd "$JARS_DIR"
zip -r "$BUILD_DIR/teamArtifacts.zip" *.jar

echo "🎉 Готово: $BUILD_DIR/teamArtifacts.zip"
