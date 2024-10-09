package org.nagp.tests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.dataProvider.Constants;
import org.nagp.dataProvider.ExcelHelper;
import org.nagp.dataProvider.TestDataReader;
import org.nagp.framework.Helper;
import org.nagp.framework.TestDriver;
import org.nagp.pages.BusSearchResultPage;
import org.nagp.pages.HelpPage;
import org.nagp.pages.Homepage;
import org.nagp.pages.TrainTicketPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.Assert.assertTrue;

public class RailwayPageTests extends TestDriver{
    private static final Logger logger = LogManager.getLogger(RailwayPageTests.class);
    TestDriver testDriver;
    private Helper helper = new Helper();
    private Map<String, String> testData;
    private final Properties configProps = helper.readConfig();
    Homepage home;
    TrainTicketPage trainTicketPage;
    private ExcelHelper excelHelper = new ExcelHelper();


    @BeforeClass(alwaysRun = true)
    private void setupTests() {
        testDriver = new TestDriver();
        testDriver.initialize();
        testDriver.setup();
        TestDataReader.init();
    }

    /**
     * Execute this method at the start of each test case.
     */
    @BeforeMethod(alwaysRun = true)
    private void start(ITestResult iTestResult) {
        iTestResult.setAttribute("webDriver", testDriver.getWebDriver());
        testDriver.getWebDriver().navigate().refresh();
        String url = configProps.getProperty("webHost");
        testDriver.getWebDriver().get(url);
        testDriver.getWebDriver().manage().window().maximize();
    }

    //Test with data reading from Excel
    @Test(description = "Verify User can see three different sections for Railways", groups = {"regression"})
    public void verifyUserCanSeeMultipleRailwayOptions() {
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        trainTicketPage = home.clickTrainPage(testDriver.getWebDriver());
        Assert.assertTrue(trainTicketPage.isOpen(testDriver.getWebDriver()),"Train Page is not opened");
        List<String> colData = excelHelper.readData("RailwayOptions", Constants.UPLOAD_PATH+"excelData.xlsx",0);
        assertTrue(trainTicketPage.getRailwayOperationsText(testDriver.getWebDriver()).containsAll(colData),"Mismatch in Railway Sections");
    }

    @Test(description = "Verify User gets error on invalid PNR entered", groups = {"regression","smoke"})
    public void verifyUserGetsErrorOnInvalidPNR() {
        testData = TestDataReader.getDataMap("verifyUserGetsErrorOnInvalidPNR");
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        trainTicketPage = home.clickTrainPage(testDriver.getWebDriver());
        Assert.assertTrue(trainTicketPage.isOpen(testDriver.getWebDriver()),"Train Page is not opened");
        trainTicketPage.clickOnCheckPNRStatusButton();
        trainTicketPage.enterPnrNumberAndClickSearch(testData.get("pnr"));
        Assert.assertEquals(trainTicketPage.getPNRErrorText(testDriver.getWebDriver()),testData.get("errorMessage"),"Mismatch in Error Message");
    }

    @Test(description = "Verify User can search and see live train status", groups = {"regression"})
    public void verifyUserCanSearchForLiveTrain() {
        testData = TestDataReader.getDataMap("verifyUserCanSearchForLiveTrain");
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        trainTicketPage = home.clickTrainPage(testDriver.getWebDriver());
        Assert.assertTrue(trainTicketPage.isOpen(testDriver.getWebDriver()),"Train Page is not opened");
        trainTicketPage.clickOnLiveTrainStatusButton();
        trainTicketPage.enterTrainNumberAndClickSearch(testData.get("trainNumber"),testDriver.getWebDriver());
        assertTrue(trainTicketPage.checkLiveTrainStatusPageIsVisible(testDriver.getWebDriver()),"Live Train Page not visible");
    }

    @Test(description = "Verify User can search and see live train History", groups = {"regression","smoke"})
    public void verifyUserCanSeeLiveTrainHistory() {
        testData = TestDataReader.getDataMap("verifyUserCanSearchForLiveTrain");
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        trainTicketPage = home.clickTrainPage(testDriver.getWebDriver());
        Assert.assertTrue(trainTicketPage.isOpen(testDriver.getWebDriver()),"Train Page is not opened");
        trainTicketPage.clickOnLiveTrainStatusButton();
        trainTicketPage.enterTrainNumberAndClickSearch(testData.get("trainNumber"),testDriver.getWebDriver());
        assertTrue(trainTicketPage.checkLiveTrainStatusPageIsVisible(testDriver.getWebDriver()),"Live Train Page not visible");
        trainTicketPage.clickOnBackButton(testDriver.getWebDriver());
        assertTrue(trainTicketPage.checkPrevSearchedTrainIsVisible(testDriver.getWebDriver()),"Previous Train Search not visible");
    }

    @AfterMethod(alwaysRun = true)
    public void goBackToHomePage() throws Exception {
        String url = configProps.getProperty("webHost");
        testDriver.getWebDriver().navigate().to(url);
        if (configProps.getProperty("consoleLogging").equalsIgnoreCase("true")) {
            List<LogEntry> logs = testDriver.getBrowserConsoleLogs(testDriver.getWebDriver());
            if (!logs.isEmpty()) {
                logger.warn("Detected browser console logs entries: {}", logs.size());
                logs.forEach(a -> logger.warn(a));
            }
        }
    }

    @AfterClass(alwaysRun = true)
    protected void tearDown() {
        testDriver.quitWebDriverForParallel(testDriver.getWebDriver());
    }


}
