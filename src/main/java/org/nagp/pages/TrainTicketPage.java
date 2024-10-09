package org.nagp.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.dataProvider.Constants;
import org.nagp.framework.WebElements;
import org.nagp.utils.WaitTool;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TrainTicketPage extends WebElements {
    WebDriver driver;
    private static final Logger logger = LogManager.getLogger(TrainTicketPage.class);

    @FindBy(xpath="//button[contains(text(),'search trains')]")
    WebElement searchTrainsButton;

    @FindBy(xpath="//*[contains(@href,'bus-tickets') and @class='nav-link ']")
    WebElement BusPageLink;

    @FindBy(xpath="//*[@class='ris-wrapper']//p[text()='Check PNR Status']/../div")
    WebElement pnrStatusRadioButton;

    @FindBy(xpath="//*[@class='ris-wrapper']//p[text()='Live Train Status']/../div")
    WebElement liveTrainStatusRadioButton;

    @FindBy(xpath="//input[@class='pnr-input-text']")
    WebElement pnrInput;

    @FindBy(xpath="//button[text()='Check Status']")
    WebElement checkStatusButton;

    @FindBy(xpath="//*[@class='err_main_header']/b")
    WebElement invalidPNR;

    @FindBy(xpath="//div[@class='lts_details_main_wrap undefined']/div")
    WebElement trainLiveStatus;

    @FindBy(xpath="//*[contains(@class,'back_arr')]")
    WebElement backButton;

    @FindBy(xpath="//*[contains(@class,'recent_pnr_carousel')]")
    WebElement prevSearch;
    public TrainTicketPage(WebDriver driver) {
        this.driver= driver;
        PageFactory.initElements(driver,this);
    }

    public boolean isOpen(WebDriver driverParallel) {
        logger.info("Checking if HomePage is open");
        boolean isOpen = false;
        if (isElementPresent(searchTrainsButton, Integer.parseInt(configProps.getProperty("avgTimeout")), driverParallel)) {
            isOpen = searchTrainsButton.isDisplayed();
        } else {
            throw new NoSuchElementException("Train Page is not displayed");
        }
        return isOpen;
    }

    public Homepage clickBusBookingPage(WebDriver driver){
        logger.info("Clicking on Bus Booking Page Page...");
        BusPageLink.click();
        return new Homepage(driver);
    }

    public List<String> getRailwayOperationsText(WebDriver driver){
        logger.info("Getting the Different Railway Operations available");
        List<String> operationsName= new ArrayList<>();
        List<WebElement> operations = getElementsByXpath("//*[@class='ris-wrapper']/div/p", driver);
        for(WebElement operation : operations){
            operationsName.add(operation.getText().trim());
        }
        return operationsName;
    }

    public void clickOnCheckPNRStatusButton(){
        logger.info("Clicking on Check PNR status button");
        pnrStatusRadioButton.click();
    }

    public void clickOnLiveTrainStatusButton(){
        logger.info("Clicking on Live Train status button");
        liveTrainStatusRadioButton.click();
    }

    public void clickOnBackButton(WebDriver driver){
        logger.info("Clicking on Back button");
        try {
            backButton.click();
        }catch (Exception e){
            logger.info("Getting exception as:"+e.getMessage());
            executeJavascript(Constants.JAVASCRIPT_CLICK_ACTION,backButton,driver);
        }

    }

    public void enterPnrNumberAndClickSearch(String text){
        logger.info("Clicking on Check PNR status button");
        pnrInput.sendKeys(text);
        checkStatusButton.click();
    }

    public void enterTrainNumberAndClickSearch(String text,WebDriver driver){
        logger.info("Entering Train text and Click Check status");
        pnrInput.sendKeys(text);
        getElementByXpathAndClick("//*[contains(@class,'lts_solr_wrap')]//*[contains(text(),'xxx')]/..".replace("xxx",text),driver);
        checkStatusButton.click();
    }

    public String getPNRErrorText(WebDriver driver){
        logger.info("Getting Error message text for invalid PNR");
        WaitTool.waitForElementVisibleByWebElement(driver,invalidPNR,Integer.parseInt(configProps.getProperty("avgTimeout")));
        return invalidPNR.getText();
    }

    public boolean checkLiveTrainStatusPageIsVisible(WebDriver driverParallel) {
        logger.info("Checking if Live Train Status Page is visible ");
        boolean isOpen = false;
        if (isElementPresent(trainLiveStatus, Integer.parseInt(configProps.getProperty("avgTimeout")), driverParallel)) {
            isOpen = trainLiveStatus.isDisplayed();
        } else {
            throw new NoSuchElementException("Train Page is not displayed");
        }
        return isOpen;
    }

    public boolean checkPrevSearchedTrainIsVisible(WebDriver driverParallel) {
        logger.info("Checking if Previous Searched Train Status Page is visible ");
        boolean isOpen = false;
        if (isElementPresent(prevSearch, Integer.parseInt(configProps.getProperty("avgTimeout")), driverParallel)) {
            isOpen = prevSearch.isDisplayed();
        } else {
            throw new NoSuchElementException("Train Page is not displayed");
        }
        return isOpen;
    }


}
