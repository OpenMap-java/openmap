@echo off

rem This will launch the OpenMap application
rem YOU MUST EDIT THE SETTINGS IN THIS FILE TO MATCH YOUR CONFIGURATION

rem Java Virtual Machine
rem CHANGE THIS TO MATCH YOUR CONFIGURATION
set JAVABIN=C:\jdk1.2\bin\java.exe

rem OpenMap top-level directory
rem CHANGE THIS TO MATCH YOUR CONFIGURATION
set OM_HOME=C:\openmap

rem CLASSPATH points to toplevel OpenMap directory and share subdirectory
set CLASSPATH=%OM_HOME%;%OM_HOME%\share;%OM_HOME%\lib\openmap.jar

rem OK, now run OpenMap
%JAVABIN% -mx64m -Dopenmap.configDir=%OM_HOME%\share com.bbn.openmap.app.OpenMap
