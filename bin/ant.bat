@echo off

if exist "%HOME%\antrc_pre.bat" call "%HOME%\antrc_pre.bat"

if not "%OS%"=="Windows_NT" goto win9xStart
:winNTStart
@setlocal

rem %~dp0 is name of current script under NT
set DEFAULT_ANT_HOME=%~dp0

rem : operator works similar to make : operator
set DEFAULT_ANT_HOME=%DEFAULT_ANT_HOME:\bin\=%

if %ANT_HOME%a==a set ANT_HOME=%DEFAULT_ANT_HOME%
set DEFAULT_ANT_HOME=

rem On NT/2K grab all arguments at once
set ANT_CMD_LINE_ARGS=%*
goto doneStart

:win9xStart
rem Slurp the command line arguments.  This loop allows for an unlimited number of 
rem agruments (up to the command line limit, anyway).

set ANT_CMD_LINE_ARGS=

:setupArgs
if %1a==a goto doneStart
set ANT_CMD_LINE_ARGS=%ANT_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart
rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.

rem find OPENMAP_HOME
if not "%OPENMAP_HOME%"=="" goto checkJava

rem check for OpenMap in Program Files on system drive
if not exist "%SystemDrive%\Program Files\openmap" goto checkSystemDrive
set OPENMAP_HOME=%SystemDrive%\Program Files\openmap
goto checkJava

:checkSystemDrive
rem check for OpenMap in root directory of system drive
if not exist "%SystemDrive%\openmap" goto noOpenMapHome
set OPENMAP_HOME=%SystemDrive%\openmap
goto checkJava

:noOpenMapHome
echo OPENMAP_HOME is not set and openmap package could not be located. Please set OPENMAP_HOME.
goto end

:checkJava
set ANT_HOME=%OPENMAP_HOME%\%OPENMAP_ANT_HOME%
set _JAVACMD=%JAVACMD%
set OPENMAP_CLASSPATH=%CLASSPATH%
for %%i in ("%OPENMAP_ANT_HOME%\lib\*.jar") do call "%OPENMAP_ANT_HOME%\bin\lcp.bat" "%%i"

if "%JAVA_HOME%" == "" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto checkJikes

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If build fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

:checkJikes
if not "%JIKESPATH%" == "" goto runAntWithJikes

:runAnt

%_JAVACMD% -classpath "%AMP_CLASSPATH%" -Dant.home="%OPENMAP_ANT_HOME%" %ANT_OPTS% org.apache.tools.ant.Main %ANT_CMD_LINE_ARGS%
goto end

:runAntWithJikes
%_JAVACMD% -classpath %AMP_CLASSPATH% -Dant.home="%OPENMAP_ANT_HOME%" -Djikes.class.path=%JIKESPATH% %ANT_OPTS% org.apache.tools.ant.Main %ANT_CMD_LINE_ARGS%

:end
set AMP_CLASSPATH=
set _JAVACMD=
set ANT_CMD_LINE_ARGS=

if not "%OS%"=="Windows_NT" goto mainEnd
:winNTend
@endlocal

:mainEnd
if exist "%HOME%\antrc_post.bat" call "%HOME%\antrc_post.bat"

