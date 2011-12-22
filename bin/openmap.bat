@echo off

rem This will launch the OpenMap application
rem YOU MUST EDIT THE SETTINGS IN THIS FILE TO MATCH YOUR CONFIGURATION

rem Java Virtual Machine
rem CHANGE THIS TO POINT TO YOUR JAVA INSTALLATION IF JAVA ISN"T IN YOUR PATH
set JAVABIN=java.exe

rem OpenMap top-level directory
rem THIS POINTS TO THE DIRECTORY ABOVE "BIN". CHANGE THIS IF YOU MOVE THE
rem BATCH FILE.
set OPENMAP_HOME=.

rem CLASSPATH points to toplevel OpenMap directory and share
rem subdirectory.  Add other jar files as available.
set CLASSPATH=%OPENMAP_HOME%;%OPENMAP_HOME%\classes\openmap;%OPENMAP_HOME%\share;%OPENMAP_HOME%\lib\milStd2525_png.jar;%OPENMAP_HOME%\lib\openmap.jar;%OPENMAP_HOME%\lib\omsvg.jar;%OPENMAP_HOME%\lib\omj3d.jar;%OPENMAP_HOME%\lib\omcorba_vb.jar;

rem OK, now run OpenMap
%JAVABIN% -Xmx64m -Dopenmap.configDir=%OPENMAP_HOME%\share -Ddebug.showprogress com.bbn.openmap.app.Main
