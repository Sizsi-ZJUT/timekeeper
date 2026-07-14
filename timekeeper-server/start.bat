@echo off
set JAVA_HOME=E:\JAVA\jdk-17.0.19+10
cd /d %~dp0
call mvnw.cmd spring-boot:run
