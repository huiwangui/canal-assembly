@echo off & setlocal enabledelayedexpansion

set LIB_JARS=""
cd ..\lib
for %%i in (*) do set LIB_JARS=!LIB_JARS!;..\lib\%%i

set PAY1PAYLIB_JARS=""

cd ..\bin

if ""%1"" == ""debug"" goto debug
if ""%1"" == ""jmx"" goto jmx

java -Xms4096m -Xmx4096m -XX:MaxPermSize=256M -classpath ..\conf;%LIB_JARS%;%PAY1PAYLIB_JARS% com.hxyc.CanalApplication
goto end

:debug
java -Xms4096m -Xmx4096m -XX:MaxPermSize=256M -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n -classpath ..\conf;%LIB_JARS%;%PAY1PAYLIB_JARS% com.hxyc.CanalApplication
goto end

:jmx
java -Xms4096m -Xmx4096m -XX:MaxPermSize=256M -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -classpath ..\conf;%LIB_JARS%;%PAY1PAYLIB_JARS% com.hxyc.CanalApplication

:end
pause