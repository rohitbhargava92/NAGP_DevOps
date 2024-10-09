package org.nagp.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.framework.Helper;
import org.nagp.framework.WebElements;
import org.nagp.utils.WaitTool;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.NoSuchElementException;

public class Homepage extends WebElements {
    WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Homepage.class);

        Helper helper= new Helper();

    @FindBy(xpath="//button[contains(text(),'SEARCH BUSES')]")
    WebElement searchBusesButton;
    @FindBy(xpath="//*[contains(@data-url,'railways')]/div")
    WebElement trainPageLink;

    @FindBy(xpath="//*[contains(@id,'autoSuggest')]//label[@for='src']/preceding-sibling::input | //*[contains(@id,'autoSuggest')]//label[@for='src']/..//text")
    WebElement fromInput;

    @FindBy(xpath="//*[contains(@id,'autoSuggest')]//label[@for='dest']/preceding-sibling::input | //*[contains(@id,'autoSuggest')]//label[@for='dest']/..//text")
    WebElement toInput;

    @FindBy(xpath="//*[@id='onwardCal']")
    WebElement dateButton;

    @FindBy(xpath="//*[@data-text='Help']/div")
    WebElement help;

    @FindBy(xpath="//a[contains(text(),'View All') and contains(@href,'Offer')]")
    WebElement offerViewAll;

    @FindBy(xpath="//td[@class='tiles offerBlock']")
    WebElement offerCarousel;

    @FindBy(xpath="//*[contains(@id,'autoSuggestContainer')]/..//h1")
    WebElement upperTagline;
    public Homepage(WebDriver driver) {
        this.driver= driver;
        PageFactory.initElements(driver,this);
    }

    public boolean isOpen(WebDriver driverParallel) {
        logger.info("Checking if HomePage is open");
        boolean isOpen = false;
        if (isElementPresent(searchBusesButton, Integer.parseInt(configProps.getProperty("avgTimeout")), driverParallel)) {
            isOpen = searchBusesButton.isDisplayed();
        } else {
            throw new NoSuchElementException("HomePage is not displayed");
        }
        return isOpen;
    }

    public TrainTicketPage clickTrainPage(WebDriver driver){
        logger.info("Clicking on Trains Page...");
        trainPageLink.click();
        return new TrainTicketPage(driver);
    }

    public String getUpperTagline(){
        logger.info("Getting upper tag line");
        return upperTagline.getText();
    }

    public void enterSearchTextOnFrom(String text,WebDriver driver){
        logger.info("Entering text into Search From....");
        fromInput.sendKeys(text);
        WaitTool.waitForElementPresentByXpath(driver,"//*[contains(@id,'autoSuggest')]//ul/li//text[text()='xxx']/..".replace("xxx",text));
        getElementByXpathAndClick("//*[contains(@id,'autoSuggest')]//ul/li//text[text()='xxx']/..".replace("xxx",text),driver);
    }

    public void enterSearchTextOnTo(String text,WebDriver driver){
        logger.info("Entering text into Search To....");
        toInput.sendKeys(text);
        WaitTool.waitForElementPresentByXpath(driver,"//*[contains(@id,'autoSuggest')]//ul/li//text[text()='xxx']/..".replace("xxx",text));
        getElementByXpathAndClick("//*[contains(@id,'autoSuggest')]//ul/li//text[text()='xxx']/..".replace("xxx",text),driver);
    }

    public void selectDateForBusTravel(){
        logger.info("Clicking on Date Button");
        int currentDate = helper.getCurrentDateOnly();
        if(!isElementPresent("//span[contains(@class,'DayTiles__CalendarDaysSpan') and text()='xxx']".replace("xxx",String.valueOf(currentDate+1)),driver)){
            dateButton.click();
        }
        getElementByXpathAndClick("//span[contains(@class,'DayTiles__CalendarDaysSpan') and text()='xxx']".replace("xxx",String.valueOf(currentDate+1)),driver);
    }

    public void selectDateForBusTravel(int days){
        logger.info("Clicking on Date Button");
        int currentDate = helper.getDateOnlyTPlusMinus(days);
        if(!isElementPresent("//span[contains(@class,'DayTiles__CalendarDaysSpan') and text()='xxx']".replace("xxx",String.valueOf(currentDate+1)),driver)){
            dateButton.click();
        }
        getElementByXpathAndClick("//span[contains(@class,'DayTiles__CalendarDaysSpan') and text()='xxx']".replace("xxx",String.valueOf(currentDate+1)),driver);
    }

    public BusSearchResultPage clickOnSearchBusesButton(WebDriver driver){
        logger.info("Clicking on Search Buses button");
        searchBusesButton.click();
        return new BusSearchResultPage(driver);
    }

    public HelpPage clickOnHelpButton(WebDriver driver){
        logger.info("Clicking on Help button");
        help.click();
        return new HelpPage(driver);
    }

    public void clickOffersViewAllButton(WebDriver driver){
        logger.info("Clicking on View ALL button under offers");
        WaitTool.waitForElementVisibleByWebElement(driver,offerViewAll,Integer.parseInt(configProps.getProperty("avgTimeout")));
        offerViewAll.click();
    }

    public boolean checkOffersCarouselPageIsPresent(WebDriver driver){
        logger.info("Checking if Offer Carousel is Present");
        boolean isOpen = false;
        if (isElementPresent(offerCarousel, Integer.parseInt(configProps.getProperty("avgTimeout")), driver)) {
            isOpen = offerCarousel.isDisplayed();
        } else {
            throw new NoSuchElementException("HomePage is not displayed");
        }
        return isOpen;
    }


}
