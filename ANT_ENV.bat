rem These environment variables are required so that Ant works correctly in Dos.
rem If you have set them, then there is no need to run this file.
rem Otherwise update the paths to reflect your environment.

rem Set OPENMAP_HOME to the top-level openmap directory, the one
rem containing the build.xml file.
set OPENMAP_HOME=C:\openmap
set JAVA_HOME=C:\j2sdk1.4.2_03

rem You only need these if you don't have ant configured to run on your system.
set ANT_HOME=%OPENMAP_HOME%\ext\apache-ant-1.6.1
set PATH=%ANT_HOME%\bin;%PATH%

rem This is optional if you already have java.exe in your path
rem set PATH=%JAVA_HOME%\bin;%PATH%
