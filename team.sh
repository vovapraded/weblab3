#!/bin/bash
set -e

# Каталоги
ROOT_DIR=$(pwd)
BUILD_DIR="$ROOT_DIR/target/team"
WARS_DIR="$BUILD_DIR/teamWars"
mkdir -p "$WARS_DIR"

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

  # Находим WAR-файл и копируем
  BUILT_WAR=$(find "$WORKTREE_DIR/target" -name '*.war' | head -n 1)
  if [ -f "$BUILT_WAR" ]; then
    cp "$BUILT_WAR" "$WARS_DIR/$(basename "$BUILT_WAR" .war)-$SHORT_HASH.war"
    echo "✅ WAR сохранён: $(basename "$BUILT_WAR" .war)-$SHORT_HASH.war"
  else
    echo "❌ WAR не найден для $SHORT_HASH"
  fi

  echo "🧹 Удаляем worktree $SHORT_HASH"
  git worktree remove --force "$WORKTREE_DIR"
done

echo "📦 Упаковка ZIP..."
cd "$WARS_DIR"
zip -r "$BUILD_DIR/teamArtifacts.zip" *.war

echo "🎉 Готово: $BUILD_DIR/teamArtifacts.zip"
