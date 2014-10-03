'@echo off

rem This will launch the OpenMap application with the BeanBox component
rem YOU MUST EDIT THE SETTINGS IN THIS FILE TO MATCH YOUR CONFIGURATION

rem Java Virtual Machine
rem CHANGE THIS TO POINT TO YOUR JAVA INSTALLATION IF JAVA ISN"T IN YOUR PATH
set JAVABIN=java.exe

rem OpenMap top-level directory
rem THIS POINTS TO THE DIRECTORY ABOVE "BIN". CHANGE THIS IF YOU MOVE THE
rem BATCH FILE.
set OPENMAP_HOME=..\..\..\..\..

rem CLASSPATH points to toplevel OpenMap directory and share subdirectory
set CLASSPATH=.;%OPENMAP_HOME%\classes\openmap;%OPENMAP_HOME%\share;%OPENMAP_HOME%\lib\openmap.jar;

rem OK, now run OpenMap
%JAVABIN% -mx64m -Dopenmap.configDir=%OPENMAP_HOME%\share com.bbn.openmap.app.OpenMap -properties %OPENMAP_HOME%\src\openmap\com\bbn\openmap\examples\beanbox\openmap.properties
