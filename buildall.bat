rem This file builds all of the OpenMap source

rem Here's where software lives
set SWINGHOME=D:\jdk1.1.6\swing-1.0.2
set OM_HOME=E:\openmap-3.0beta2
set CLASSPATH=%CLASSPATH%;%SWINGHOME%\swing.jar;%OM_HOME%

rem Here's where the compiler lives and the flags we like to give it
set JAVAC=D:\jdk1.1.6\bin\javac
set JAVAFLAGS= -g -deprecation

%JAVAC% %JAVAFLAGS% *.java
%JAVAC% %JAVAFLAGS% app\*.java
%JAVAC% %JAVAFLAGS% event\*.java
%JAVAC% %JAVAFLAGS% examples\applet\*.java
%JAVAC% %JAVAFLAGS% examples\crew\*.java
%JAVAC% %JAVAFLAGS% examples\hello\*.java
%JAVAC% %JAVAFLAGS% examples\javaPlugIn\*.java
%JAVAC% %JAVAFLAGS% gifMapBean\*.java
%JAVAC% %JAVAFLAGS% gui\*.java
%JAVAC% %JAVAFLAGS% layer\*.java
%JAVAC% %JAVAFLAGS% layer\dted\*.java
%JAVAC% %JAVAFLAGS% layer\nitf\*.java
%JAVAC% %JAVAFLAGS% layer\plotLayer\*.java
%JAVAC% %JAVAFLAGS% layer\pluginLayer\*.java
%JAVAC% %JAVAFLAGS% layer\shape\*.java
%JAVAC% %JAVAFLAGS% layer\util\cacheHandler\*.java
%JAVAC% %JAVAFLAGS% layer\util\html\*.java
%JAVAC% %JAVAFLAGS% layer\util\http\*.java
%JAVAC% %JAVAFLAGS% layer\util\stateMachine*.java
%JAVAC% %JAVAFLAGS% layer\vpf\*.java
%JAVAC% %JAVAFLAGS% omGraphics\*.java
%JAVAC% %JAVAFLAGS% proj\*.java
%JAVAC% %JAVAFLAGS% util\*.java
