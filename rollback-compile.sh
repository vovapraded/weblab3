#!/bin/bash
set -e

echo "🔁 Starting rollback-compile for Gradle project..."

ROOT_DIR=$(pwd)
DIFF_FILE="$ROOT_DIR/diff.txt"
BACKUP_COMMIT=""
MAX_COMMITS=30

function compile_project() {
  echo "⚙️ Compiling with Gradle..."
  gradle clean compileJava --no-daemon --quiet
}

function backup_changes() {
  echo "💾 Creating backup commit of current changes..."
  git add -A
  if git diff --cached --quiet && git diff --quiet; then
    echo "⚠ No changes to back up."
  else
    git commit -m "rollback-backup-temp" || echo "⚠ Backup commit skipped (maybe already committed)"
    BACKUP_COMMIT=$(git rev-parse HEAD)
    echo "✅ Backup commit: $BACKUP_COMMIT"
  fi
}

function generate_diff() {
  local good=$1
  local bad=$2
  echo "📝 Generating diff between $good and $bad (only src/)"
  git diff "$good" "$bad" -- src/ > "$DIFF_FILE"
  echo "✅ Diff saved to $DIFF_FILE"
}

function clean_workspace() {
  echo "🧹 Cleaning workspace..."
  git clean -xdf > /dev/null
}

# Step 1: Try current commit
if compile_project; then
  echo "✅ Project compiles on current commit: $(git rev-parse HEAD)"
  exit 0
fi

# Step 2: Backup and collect history
backup_changes
COMMITS=($(git rev-list --max-count=$MAX_COMMITS HEAD))
LAST_GOOD=""
NEXT_BAD=""

# Step 3: Walk through history
for ((i=1; i<${#COMMITS[@]}; i++)); do
  commit=${COMMITS[$i]}
  short=$(echo "$commit" | cut -c1-7)
  echo "🔄 Trying commit: $short"
  git checkout "$commit" --quiet -- src/
  clean_workspace
  if compile_project; then
    LAST_GOOD=$commit
    NEXT_BAD=${COMMITS[$((i-1))]}
    echo "✅ Found last working commit: $LAST_GOOD"
    break
  else
    echo "❌ Failed to compile: $short"
  fi
done

# Step 4: Diff and restore
if [[ -n "$LAST_GOOD" ]]; then
  if [[ -n "$NEXT_BAD" ]]; then
    generate_diff "$LAST_GOOD" "$NEXT_BAD"
  elif [[ -n "$BACKUP_COMMIT" ]]; then
    generate_diff "$LAST_GOOD" "$BACKUP_COMMIT"
  else
    echo "⚠ No commit to diff against"
  fi
else
  echo "🚨 No working commit found in last $MAX_COMMITS commits."
fi

# Step 5: Restore back to main
echo "↩️ Restoring working directory to main"
git clean -xdf
git checkout main --quiet
clean_workspace
echo "🎉 Done."
