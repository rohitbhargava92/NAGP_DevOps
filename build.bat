
@echo off

:: Assumes Maven is already in your PATH environment variable!!
echo %JAVA_HOME%
mvn clean package -DskipTests=true
