rem This file works with OpenMap 3.1.2 or greater using Java 1.2 (Java 2)
rem   This file run the OpenMap application

rem Set up some environment variables

rem This is where the OpenMap top-level directories are.
rem CHANGE THIS TO MATCH YOUR CONFIGURATION
set OM_HOME=C:\home\adoyle\openmap

rem This assumes your CLASSPATH is already set
set CLASSPATH=%CLASSPATH%;%OM_HOME%;%OM_HOME%\share
set JAVABIN=java

rem OK, now run it
%JAVABIN% -mx64m -Dopenmap.configDir=%OM_HOME%\share com.bbn.openmap.app.OpenMap
