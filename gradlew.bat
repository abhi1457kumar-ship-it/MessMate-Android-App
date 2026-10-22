@echo off
setlocal
set MM_PROJECT_ROOT=%~dp0
set MM_WRAPPER_JAR=%MM_PROJECT_ROOT%gradle\wrapper\gradle-wrapper.jar
if not exist "%MM_WRAPPER_JAR%" (
  echo Missing Gradle wrapper JAR: %MM_WRAPPER_JAR%
  exit /b 1
)
java -classpath "%MM_WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
