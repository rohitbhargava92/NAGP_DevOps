# Nagp_Selenium-1.0-SNAPSHOT.jar

Contains UI tests for the RedBus application based on Java, Selenium
WebDriver, and TestNG. It is built based on a page object design.
1. The test Data is maintained in testCaseData.xml and is read via XMLParser
2. Regression and Smoke xml are there to run test cases bases on need
3. Screenshots and extent reports are generated automatically and are placed in Current Test Results. Old data can be seen on Archived Test Result Package
4. One test is automated using Data provider, one has data from excel and others take data from 

## PRE-REQUISITES:

1. Make sure that Java, TestNG, Maven are installed in the system already.
2. Drivers are taken automatically from Webdriver Manager
3. The test results for current run are saved in CurrentTestResults folder and automatically moved to ArchivedTestResult folder when new run is started

## RUNNING TESTS:

1. Always check the **config.properties** file settings and make changes as needed.
2. You can also use Maven command line to run test cases. mvn clean test. It runs smokeTest.xml which is defined in pom. Example mvn clean test
3. Build the automation project with the included scripts: _build.sh_ or _build.bat_
4. Tests can be triggered from regression.xml or smoke.xml
5. To retry any failed test case, change setiing on config.properties file to true and false and also set the count

