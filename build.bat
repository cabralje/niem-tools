@echo off
rem echo Generating NIEM 3.2 reference model JAXB bindings
rem xjc -verbose -catalog ..\xml\niem-3.2\xml-catalog.xml -b niem-3.2\bindings.xml ..\xml\niem-3.2\niem -d niem-3.2\src
rem for /r ..\xml\niem-3.2\niem %%i in (*.xsd) do ( echo %%i & xjc %%i -b niem-3.2\bindings.xml -d niem-3.2\src )

rem echo Compiling NIEM 3.2 reference model JAXB bindings
rem for /r niem-3.2\src %%i in (*.java) do ( echo %%i & javac %%i -sourcepath niem-3.2\src -d niem-3.2\bin )

rem echo Creating NIEM 3.2 jar
rem jar -cf niem-3.2.jar -C niem-3.2\bin .

rem echo Generating ECF 4.01 JAXB bindings
rem xjc ..\xml\ecf-4.01\xsd -d ecf-4.01\src
rem for /r ..\xml\ecf-4.01\xsd %%i in (*.xsd) do ( echo %%i & xjc %%i -d ecf-4.01\src )

rem echo Compiling ECF 4.01 JAXB bindings
rem for /r ecf-4.01\src %%i in (*.java) do ( echo %%i & javac %%i -sourcepath ecf-4.01\src -d ecf-4.01\bin )

rem echo Creating ECF 4.01 jar
rem jar -cf ecf-4.01.jar -C ecf-4.01\bin .

rem echo Generate NIEM Wantlist 2.2 JAXB binding
rem xjc ..\xml\wantlist-2.2-annotated.xsd -d wantlist-2.2\src

rem echo Compiling NIEM wantlist 2.2 JAXB bindings
rem for /r wantlist-2.2\src %%i in (*.java) do ( echo %%i & javac %%i -sourcepath wantlist-2.2\src -d wantlist-2.2\bin )

rem echo Creating NIEM Wantlist 2.2 jar
rem jar -cf wantlist-2.2.jar -C wantlist-2.2\bin .

echo Compiling NIEM tools
rem javac -cp opencsv-3.5.jar;wantlist-2.2.jar -d bin src\*.java
javac -cp opencsv-3.5.jar -d bin src\*.java

echo Creating NIEM tools jar
jar -cf niemtools.jar -C bin .
