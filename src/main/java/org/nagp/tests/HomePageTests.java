package org.nagp.tests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.dataProvider.TestDataReader;
import org.nagp.framework.Helper;
import org.nagp.framework.TestDriver;
import org.nagp.pages.BusSearchResultPage;
import org.nagp.pages.HelpPage;
import org.nagp.pages.Homepage;
import org.nagp.pages.TrainTicketPage;
import org.openqa.selenium.logging.LogEntry;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HomePageTests extends TestDriver {
    private static final Logger logger = LogManager.getLogger(HomePageTests.class);
    TestDriver testDriver;
    private Helper helper = new Helper();
    private Map<String, String> testData;
    private final Properties configProps = helper.readConfig();
    Homepage home;
    TrainTicketPage trainTicketPage;

    BusSearchResultPage busSearchResultPage;

    HelpPage helpPage;

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
    private void start(ITestResult iTestResult, Method method) {
        iTestResult.setAttribute("webDriver", testDriver.getWebDriver());
        testDriver.getWebDriver().navigate().refresh();
        String url = configProps.getProperty("webHost");
        testDriver.getWebDriver().get(url);
        testDriver.getWebDriver().manage().window().maximize();
    }

    @DataProvider (name = "travelDetails")
    public Object[][] travelData(){
        return new Object[][] {{"Delhi", "Agra" , 1}, {"Haridwar", "Delhi" , 2},{"Mathura", "Agra" , 3}};
    }

    @Test(dataProvider ="travelDetails", description = "Verify User can toggle between Bus and Train tickets using Data providers", groups = {"regression"})
    public void verifyUserSearchForBusesBetweenTwoPlacesUsingDataProviders(String from, String to, int days) {
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        home.enterSearchTextOnFrom(from,testDriver.getWebDriver());
        home.enterSearchTextOnTo(to,testDriver.getWebDriver());
        home.selectDateForBusTravel(days);
        busSearchResultPage = home.clickOnSearchBusesButton(testDriver.getWebDriver());
        Assert.assertTrue(busSearchResultPage.isOpen(testDriver.getWebDriver()),"Unable to find Bus Search result Page");
    }

    @Test(description = "Verify User can toggle between Bus and Train tickets", groups = {"regression"})
    public void verifyUserCanToggleBetweenBusTrainTickets() {
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        trainTicketPage = home.clickTrainPage(testDriver.getWebDriver());
        Assert.assertTrue(trainTicketPage.isOpen(testDriver.getWebDriver()),"Train Page is not opened");
        trainTicketPage.clickBusBookingPage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
    }

    //Below Test case is Failed Intentionally
    @Test(description = "Verify User can check Tagline of Redbus", groups = {"regression","failed"})
    public void verifyUserCanCheckTagline() {
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        Assert.assertEquals(home.getUpperTagline(),"India's No. 2 Online Bus Ticket Booking Site","Mismatch in Upper Tagline");
    }

    @Test(description = "Verify User can toggle between Bus and Train tickets", groups = {"regression","smoke"})
    public void verifyUserCanSearchForBusesBetweenTwoPlaces() {
        testData = TestDataReader.getDataMap("verifyUserCanSearchForBusesBetweenTwoPlaces");
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        home.enterSearchTextOnFrom(testData.get("busFrom").toString(),testDriver.getWebDriver());
        home.enterSearchTextOnTo(testData.get("busTo").toString(),testDriver.getWebDriver());
        home.selectDateForBusTravel(1);
        busSearchResultPage = home.clickOnSearchBusesButton(testDriver.getWebDriver());
        Assert.assertTrue(busSearchResultPage.isOpen(testDriver.getWebDriver()),"Unable to find Bus Search result Page");
    }

    @Test(description = "Verify User can go to Help Section", groups = {"regression"})
    public void verifyUserCanCheckHelpSection() {
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        helpPage = home.clickOnHelpButton(testDriver.getWebDriver());
        testDriver.getWebDriver().switchTo().window(new ArrayList<>(testDriver.getWebDriver().getWindowHandles()).get(1));
        Assert.assertTrue(helpPage.isOpen(testDriver.getWebDriver()),"HelpPage is not opened");
        testDriver.getWebDriver().close();
        testDriver.getWebDriver().switchTo().window(new ArrayList<>(testDriver.getWebDriver().getWindowHandles()).get(0));
    }

    @Test(description = "Verify User can see offer carousel", groups = {"regression"})
    public void verifyUserCanSeeOffersCarousel() {
        home= new Homepage(testDriver.getWebDriver());
        Assert.assertTrue(home.isOpen(testDriver.getWebDriver()),"HomePage is not opened");
        home.clickOffersViewAllButton();
        testDriver.getWebDriver().switchTo().window(new ArrayList<>(testDriver.getWebDriver().getWindowHandles()).get(1));
        Assert.assertTrue(home.checkOffersCarouselPageIsPresent(testDriver.getWebDriver()),"Offer Carousel is not opened");
        testDriver.getWebDriver().close();
        testDriver.getWebDriver().switchTo().window(new ArrayList<>(testDriver.getWebDriver().getWindowHandles()).get(0));
    }

    @AfterMethod(alwaysRun = true)
    public void goBackToHomePage() throws Exception {
        String url = configProps.getProperty("webHost");
        testDriver.getWebDriver().manage().deleteAllCookies();
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
