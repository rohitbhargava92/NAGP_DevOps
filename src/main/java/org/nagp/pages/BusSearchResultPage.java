package org.nagp.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.framework.WebElements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.NoSuchElementException;

public class BusSearchResultPage extends WebElements {
    WebDriver driver;
    private static final Logger logger = LogManager.getLogger(BusSearchResultPage.class);

    @FindBy(xpath="//*[@class='bus-items']")
    WebElement busSearchResultContainer;

    public BusSearchResultPage(WebDriver driver) {
        this.driver= driver;
        PageFactory.initElements(driver,this);
    }

    public boolean isOpen(WebDriver driverParallel) {
        logger.info("Checking if Bus Search Result is open");
        boolean isOpen = false;
        if (isElementPresent(busSearchResultContainer, Integer.parseInt(configProps.getProperty("avgTimeout")), driverParallel)) {
            isOpen = busSearchResultContainer.isDisplayed();
        } else {
            throw new NoSuchElementException("Train Page is not displayed");
        }
        return isOpen;
    }


}
