rem This file run the OpenMap application

rem Set up some environment variables
set SWINGHOME=D:\jdk1.1.6\swing-1.0.2
set OM_HOME=E:\openmap-3.0beta2
set CLASSPATH=%CLASSPATH%;%SWINGHOME%\swing.jar;%OM_HOME%;%OM_HOME%\share
set JAVABIN=java

rem OK, now run it
%JAVABIN% -mx64m -Dopenmap.configDir=%OM_HOME%\share com.bbn.openmap.app.OpenMap
