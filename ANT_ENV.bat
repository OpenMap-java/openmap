rem These environment variables are required so that Ant works correctly in Dos.
rem If you have set them, then there is no need to run this file.
rem Otherwise update the paths to reflect your environment.

set ANT_HOME=C:\jakarta-ant-1.4.1
set JAVA_HOME=C:\jdk1.3.1_01
set PATH=%ANT_HOME%\bin;%PATH%
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\crimson.jar;%ANT_HOME%\lib\jaxp.jar

rem This is optional if you already have java.exe in your path
rem set PATH=%JAVA_HOME%\bin;%PATH%