#!/bin/sh
MM_PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
MM_WRAPPER_JAR="$MM_PROJECT_ROOT/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$MM_WRAPPER_JAR" ]; then
  echo "Missing Gradle wrapper JAR: $MM_WRAPPER_JAR" >&2
  exit 1
fi
exec java -classpath "$MM_WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
