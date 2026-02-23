#!/usr/bin/env sh

# Gradle wrapper startup script

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "${0%/*}" && pwd)

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
