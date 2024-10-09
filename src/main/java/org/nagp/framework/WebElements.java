package org.nagp.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.dataProvider.Constants;
import org.nagp.utils.WaitTool;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class WebElements extends Driver {
  private static final Logger logger = LogManager.getLogger(WebElements.class);

  public WebElements(String... params) {
    super(params);
  }

  /** Get the WebElement matching the given xpath using the configured minimum timeout.
   *
   * @param xpath Xpath to use to find the element.
   * @return web element
   */
  public WebElement getElementByXpath(String xpath) {
    return getElementByXpath(xpath, Integer.parseInt(configProps.getProperty("avgTimeout")));
  }

  /**
   * this method is for parallel execution
   * Get the WebElement matching the given xpath using the configured minimum timeout.
   *
   * @param xpath Xpath to use to find the element.
   * @return web element
   */
  public synchronized WebElement getElementByXpath(String xpath,WebDriver driverParallel) {
    WaitTool.waitForElementPresentByXpath(driverParallel,xpath, Integer.parseInt(configProps.getProperty("minTimeout")));
    return getElementByXpath(xpath, Integer.parseInt(configProps.getProperty("avgTimeout")),driverParallel);
  }

  public synchronized void getElementByXpathAndClick(String xpath,WebDriver driverParallel) {
    WaitTool.waitForElementPresentByXpath(driverParallel,xpath, Integer.parseInt(configProps.getProperty("minTimeout")));
    try {
      this.executeJavascript(Constants.JAVASCRIPT_CLICK_ACTION, getElementByXpath(xpath, Integer.parseInt(configProps.getProperty("avgTimeout")), driverParallel), driverParallel);
    }
    catch(StaleElementReferenceException| ElementClickInterceptedException|TimeoutException | NullPointerException e) {
      logger.error("retrying again as could not find the element " + " - after waiting for: " + Integer.parseInt(configProps.getProperty("avgTimeout")) + "s");
      retryFindElement(xpath,driverParallel).click();
    } catch(Exception e) {
      logger.error("Unexpected error occured",e);
    }
  }

  /** Get the WebElement matching the given xpath.
   * It is preferred to call getElementByXpath(xpath) and reserve this method for special situations.
   * @param xpath Xpath to use to find the element.
   * @param timeout number of seconds to wait before timing out
   * @return web element
   */
  public WebElement getElementByXpath(String xpath, int timeout) {
    WebElement element=null;
    try {
      element = WaitTool
              .waitForElementPresent(getWebDriverInstance(), By.xpath(xpath), timeout);
      if (element == null) {
        throw new NoSuchElementException(xpath);
      }
    } catch(StaleElementReferenceException| ElementClickInterceptedException|TimeoutException e) {
      logger.error("retrying again as could not find the element " + " - after waiting for: " + timeout + "s");
      element=retryFindElement(xpath);
    }
    catch(Exception e) {
      logger.error("retrying again as could not find the element " + " - after waiting for: " + timeout + "s");
    }
    if (element == null) {
      logger.info("Element not found is {}",By.xpath(xpath));
    }
    return element;
  }


  /**
   * This method is for parallel execution
   * Get the WebElement matching the given xpath.
   * It is preferred to call getElementByXpath(xpath) and reserve this method for special situations.
   * @param xpath Xpath to use to find the element.
   * @param timeout number of seconds to wait before timing out
   * @return web element
   */
  public WebElement getElementByXpath(String xpath, int timeout,WebDriver driverParallel) {
    WebElement element = null;
    try {
      element = WaitTool
              .waitForElementPresent(driverParallel, By.xpath(xpath), timeout);
      if (element == null) {
        throw new NoSuchElementException(xpath);
      }
    } catch(StaleElementReferenceException| ElementClickInterceptedException|TimeoutException e) {
      logger.error("retrying again as could not find the element " + " - after waiting for: " + timeout + "s");
      element=retryFindElement(xpath,driverParallel);

    } catch(Exception e) {
      logger.error("Unexpected error occured",e);
    }
    if (element == null) {
      logger.info("Element not found is {}",By.xpath(xpath));
    }
    return element;
  }

  public List<String> getElementsTextAsListOfString (String locator, WebDriver driverParallel) {
    List<String> elementList = new ArrayList<>();
    List<WebElement> elementsByXpath = getElementsByXpath(locator, driverParallel);
    if (elementsByXpath == null && elementsByXpath.size() == 0){
      logger.info("Unable to found any web-elements for locator: " + locator);
      throw new NoSuchElementException("Unable to found any web-elements for locator: " + locator);
    }else{
      try {
        for (WebElement element: getElementsByXpath(locator, driverParallel)){
          if(getTextValue(element).contains("\n")){
            // Replaces next line with space so that TestData can be matched
            elementList.add(getTextValue(element).replace("\n", " ").trim());
          }else{
            elementList.add(getTextValue(element).trim());
          }
        }
      }catch (Exception e){
        logger.error("Unexpected exception at method getElementsTextAsListOfString",e);
      }
    }
    return elementList;
  }

  public List<String> getElementsColorAsListOfString (String locator, WebDriver driverParallel) {
    List<String> elementList = new ArrayList<>();
    List<WebElement> elementsByXpath = getElementsByXpath(locator, driverParallel);
    if (elementsByXpath == null && elementsByXpath.size() == 0){
      logger.info("Unable to found any web-elements for locator: " + locator);
      throw new NoSuchElementException("Unable to found any web-elements for locator: " + locator);
    }else {
      try {
        for (WebElement element : getElementsByXpath(locator, driverParallel)) {
          String text[] = element.getAttribute("style").split(";");
          if(text.length>0 && text[0]!=null){
            elementList.add(text[0].substring(text[0].lastIndexOf("_") + 1).trim().replace(")", "").trim());
          }
          else {
            logger.error("Unable to find text under style Attribute");
          }
        }
      }catch (Exception e){
        logger.error("Unexpected exception at method getElementsColorAsListOfString",e);
      }
    }
    return elementList;
  }


  public WebElement retryFindElement(String xpath) {
    WebElement element = null;
    int attempts = 0;
    int maxTries = Integer.parseInt(configProps.getProperty("maxRetries"));
    while(attempts< maxTries) {
      try {
        logger.info("number of attempts done to search element {}",attempts);
        element = WaitTool
                .waitForElementPresent(getWebDriverInstance(), By.xpath(xpath), Integer.parseInt(configProps.getProperty("minTimeout")));
        if (element == null) {
          throw new NoSuchElementException(xpath);
        }

      } catch(StaleElementReferenceException|ElementClickInterceptedException e) {
        if(attempts == maxTries)
          throw e;
      }catch(TimeoutException e) {
        if(attempts == maxTries)
          throw e;
      }
      attempts++;
    }
    return element;
  }


  /**
   *
   * This method is for parallel execution purpose retry to find element
   * @param xpath
   * @return
   */


  public WebElement retryFindElement(String xpath,WebDriver driverParallel) {
    WebElement element = null;
    int attempts = 0;
    int maxTries = Integer.parseInt(configProps.getProperty("maxRetries"));
    while(attempts< maxTries) {
      try {
        logger.info("number of attempts done to search element {}",attempts);
        element = WaitTool
                .waitForElementPresent(driverParallel, By.xpath(xpath), Integer.parseInt(configProps.getProperty("avgTimeout")));
        if(element != null) {
          return element;
        }
        else {
          logger.info("element not found retrying again {}", attempts);
        }


        if (element == null && attempts > maxTries - 2)
        {
          logger.error("Element not found  -- " + xpath);
        }

      }
      catch (NullPointerException nullPointerException)
      {
        logger.error(" EIQ ######## -- element - $ {} $ found on instance during retryFindElement null pointer exception -- {}",element.getTagName(), attempts);
        return element;
      }
      catch(StaleElementReferenceException|ElementClickInterceptedException e) {
        if(attempts == maxTries)
          throw e;
      }catch(TimeoutException e) {
        if(attempts == maxTries)
          throw e;
      }catch (Exception e)
      {
        logger.error("Unexpected exception at method retryFindElement",e);
      }
      attempts++;
    }
    return element;
  }

  public WebElement retryElementEnabled(String xpath,WebDriver driverParallel) {
    WebElement element = null;
    int attempts = 0;
    int maxTries = Integer.parseInt(configProps.getProperty("maxRetries"));
    while(attempts< maxTries) {
      try {
        logger.info("number of attempts done to search element {}",attempts);
        element = WaitTool
                .waitForElementClickable(driverParallel, By.xpath(xpath), Integer.parseInt(configProps.getProperty("avgTimeout")));

        if(element!=null ) {
          return element;

        }else
        {
          logger.info("element not found retrying again {}", attempts);
        }

        if (element == null && attempts > maxTries - 2)
        {
          throw new NoSuchElementException("Element not found  -- " + xpath);
        }

      } catch(StaleElementReferenceException|ElementClickInterceptedException e) {
        if(attempts == maxTries)
          logger.error("Element not found  -- " + xpath);
      }catch(TimeoutException e) {
        if(attempts == maxTries)
          logger.error("Element not found  -- " + xpath);
      }catch (Exception e)
      {
        logger.error("Unexpected exception at method retryElementEnabled",e);
      }
      attempts++;
    }
    return element;
  }

  /** Get the webElement that has the active focus.
   *
   * @return Element that has focus
   */
  public WebElement getFocusedElement() {
    return getWebDriverInstance().switchTo().activeElement();
  }

  /** Get the list of WebElements matching the given xpath using the configured minimum timeout.
   *
   * @param xpath Xpath to use to find elements.
   * @return list of web elements
   */
  public List<WebElement> getElementsByXpath(String xpath) {
    return getElementsByXpath(xpath, Integer.parseInt(configProps.getProperty("minTimeout")));
  }



  /** Get the list of WebElements matching the given xpath using the configured minimum timeout.
   *
   * @param xpath Xpath to use to find elements.
   * @return list of web elements
   */
  public List<WebElement> getElementsByXpath(String xpath , WebDriver driverParallel) {
    return getElementsByXpath(xpath, Integer.parseInt(configProps.getProperty("maxTimeout")),driverParallel);
  }



  /** Get the list of WebElements matching the given xpath.
   * It is preferred to call getElementsByXpath(xpath) and reserve this method for special situations.
   * @param xpath Xpath to use to find elements.
   * @param timeout number of seconds to wait before timing out
   * @return list of web elements
   */
  public List<WebElement> getElementsByXpath(String xpath, int timeout) {
    List<WebElement> list = null;
    try{
      list = WaitTool
              .waitForListElementsPresent(getWebDriverInstance(), By.xpath(xpath), timeout);
      if (list == null) {
        throw new NoSuchElementException(xpath);
      }
    } catch(StaleElementReferenceException|ElementClickInterceptedException e) {
      logger.error("retrying again as could not find the list element " + " - after waiting for: " + timeout + "s");
      list =retryFindElements(xpath);
    } catch(TimeoutException e) {
      logger.error("retrying again as could not find the list element " + " - after waiting for: " + timeout + "s");
      list=retryFindElements(xpath);
    }
    if (list == null) {
      logger.info("list element not found is {}",By.xpath(xpath));
    }
    return list;
  }

  /**
   * this method for parallel exeuction
   * Get the list of WebElements matching the given xpath.
   *     It is preferred to call getElementsByXpath(xpath) and reserve this method for special situations.
   * @param xpath
   * @param timeout
   * @return
   */

  public List<WebElement> getElementsByXpath(String xpath, int timeout, WebDriver driverParallel) {
    List<WebElement> list = null;
    try{
      list = WaitTool
              .waitForListElementsPresent(driverParallel, By.xpath(xpath), timeout);
      if (list == null) {
        throw new NoSuchElementException(xpath);
      }
    } catch(StaleElementReferenceException|ElementClickInterceptedException e) {
      logger.error("retrying again as could not find the list element " + " - after waiting for: " + timeout + "s");
      list =retryFindElements(xpath,driverParallel);
    } catch(TimeoutException e) {
      logger.error("retrying again as could not find the list element " + " - after waiting for: " + timeout + "s");
      list=retryFindElements(xpath,driverParallel);
    }
    if (list == null) {
      logger.info("list element not found is {}",By.xpath(xpath));
    }
    return list;
  }

  public List<WebElement> retryFindElements(String xpath) {
    List<WebElement> list = null;
    int attempts = 0;
    int maxTries = Integer.parseInt(configProps.getProperty("maxRetries"));
    while(attempts< maxTries) {
      try {
        logger.info("number of attempts done to search  list element {}",attempts);
        list = WaitTool
                .waitForListElementsPresent(getWebDriverInstance(), By.xpath(xpath), Integer.parseInt(configProps.getProperty("minTimeout")));
        if (list == null) {
          throw new NoSuchElementException(xpath);
        }

      } catch(StaleElementReferenceException|ElementClickInterceptedException e) {
        if(attempts == maxTries)
          throw e;
      }catch(TimeoutException e) {
        if(attempts == maxTries)
          throw e;
      }
      attempts++;
    }
    return list;
  }

  /**
   * This method is for parallel execution , retry list webelements
   * @param xpath
   * @return
   */
  public List<WebElement> retryFindElements(String xpath, WebDriver driverParallel) {
    List<WebElement> list = null;
    int attempts = 0;
    int maxTries = Integer.parseInt(configProps.getProperty("maxRetries"));
    while(attempts< maxTries) {
      try {
        logger.info("number of attempts done to search  list element {}",attempts);
        list = WaitTool
                .waitForListElementsPresent(driverParallel, By.xpath(xpath), Integer.parseInt(configProps.getProperty("minTimeout")));
        if (list == null) {
          throw new NoSuchElementException(xpath);
        }

      } catch(StaleElementReferenceException|ElementClickInterceptedException e) {
        if(attempts == maxTries)
          throw e;
      }catch(TimeoutException e) {
        if(attempts == maxTries)
          throw e;
      }
      attempts++;
    }
    return list;
  }


  /** Get the list of WebElements matching the given xpath using the configured minimum timeout.
   *
   * @param locatorTypeAndLocatorValue Xpath to use to find elements.
   * @return list of web elements
   */
  public WebElement getElement(By locatorTypeAndLocatorValue) {
    return getElement(locatorTypeAndLocatorValue, Duration.ofSeconds(Integer.parseInt(configProps.getProperty("minTimeout"))));
  }

  /** Get the list of WebElements matching the given user defined params.
   * @param locatorTypeAndLocatorValue Xpath to use to find elements.
   * @param timeout number of seconds to wait before timing out
   * @return list of web elements
   */
  public WebElement getElement(By locatorTypeAndLocatorValue, Duration timeout) {
    WebDriver driver = getWebDriverInstance();
    // nullify implicitlyWait
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    WebDriverWait wait = new WebDriverWait(driver, timeout);
    WebElement element = wait.until(x -> x.findElement(locatorTypeAndLocatorValue));
    Assert.assertTrue(element!=null && element.isEnabled());
    return element;
  }
  /**
   /**
   * Checks for presence of element with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @return true if element is present false otherwise
   */

  public static boolean isElementEnabled(By locatorTypeAndLocatorValue) {
    return isElementEnabled(locatorTypeAndLocatorValue, Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
  }
  /**
   * Checks for presence of element with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @param timeout timeout to wait for element
   * @return true if element is present false otherwise
   */
  public static boolean isElementEnabled(By locatorTypeAndLocatorValue, Duration timeout) {
    boolean present = false;
    try {
      WebDriver driver = getWebDriverInstance();
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      WebElement element = wait.until(x -> x.findElement(locatorTypeAndLocatorValue));
      if (element.isEnabled()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(locatorTypeAndLocatorValue+"not found"+e.getStackTrace());
    }
    return present;
  }

  /**
   /**
   * Checks if element is displayed with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @return true if element is present false otherwise
   */

  public static boolean isElementDisplayed(By locatorTypeAndLocatorValue) {
    return isElementDisplayed(locatorTypeAndLocatorValue, Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
  }

  /**
   /**
   * Checks if element is displayed with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * this method for parallel processing
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @return true if element is present false otherwise
   */
  public static boolean isElementDisplayed(By locatorTypeAndLocatorValue,WebDriver driverParallel) {
    return isElementDisplayed(locatorTypeAndLocatorValue, Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))), driverParallel);
  }
  /**
   * Checks for presence of element with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @param timeout timeout to wait for element
   * @return true if element is present false otherwise
   */
  public static boolean isElementDisplayed(By locatorTypeAndLocatorValue, Duration timeout) {
    boolean present = false;
    try {
      WebDriver driver = getWebDriverInstance();
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      WebElement element = wait.until(x -> x.findElement(locatorTypeAndLocatorValue));
      if (element.isDisplayed()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(locatorTypeAndLocatorValue+"not found"+e.getStackTrace());
    }
    return present;
  }


  /**
   * Checks for presence of element with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * this method for parallel processing
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @param timeout timeout to wait for element
   * @return true if element is present false otherwise
   */
  public static boolean isElementDisplayed(By locatorTypeAndLocatorValue, Duration timeout, WebDriver driverParallel) {
    boolean present = false;
    try {
      WebDriver driver = driverParallel;
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      WebElement element = wait.until(x -> x.findElement(locatorTypeAndLocatorValue));
      if (element.isDisplayed()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(locatorTypeAndLocatorValue+"not found"+e.getStackTrace());
    }
    return present;
  }



  /**
   /**
   * Checks if element is selected with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @return true if element is present false otherwise
   */

  public static boolean isElementSelected(By locatorTypeAndLocatorValue) {
    return isElementSelected(locatorTypeAndLocatorValue, Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
  }
  /**
   * Checks if element is selected with user defined locators type and locator value It will search for
   * element and waits for it until specified timeout.
   * @param locatorTypeAndLocatorValue Locator Type & locator value is used to find the element of choice.
   * @param timeout timeout to wait for element
   * @return true if element is present false otherwise
   */
  public static boolean isElementSelected(By locatorTypeAndLocatorValue, Duration timeout) {
    boolean present = false;
    try {
      WebDriver driver = getWebDriverInstance();
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      WebElement element = wait.until(x -> x.findElement(locatorTypeAndLocatorValue));
      if (element.isSelected()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(locatorTypeAndLocatorValue+"not found"+e.getStackTrace());
    }
    return present;
  }
  /**
   * Checks if element is selected with user defined locators type and locator value It will search for
   * Checks for presence of List of elements with specified xpath on UI.
   * Driver doesn't wait for expected elements on screen.
   *
   * @param locatorTypeAndLocatorValue xpath expression of elements to be searched on UI.
   * @return true if elements are present false otherwise
   */
  public static boolean isListOfElementsPresent(By locatorTypeAndLocatorValue) {
    return isListOfElementsPresent(locatorTypeAndLocatorValue, Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
  }
  /**
   * Checks if element is selected with user defined locators type and locator value It will search for
   * Checks for presence of List of elements with specified xpath on UI.
   * Driver doesn't wait for expected elements on screen.
   *
   * @param locatorTypeAndLocatorValue xpath expression of elements to be searched on UI.
   * @return true if elements are present false otherwise
   */
  public static boolean isListOfElementsPresent(By locatorTypeAndLocatorValue, Duration timeout) {
    boolean found = false;
    try {
      WebDriver driver = getWebDriverInstance();
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver,timeout);
      List<WebElement> list = wait.until(x -> x.findElements(locatorTypeAndLocatorValue));

      if (list == null || list.isEmpty()) {
        found = false;
      } else {
        found = true;
      }
    } catch (TimeoutException e) {
      logger.debug("TimeoutException");
    } catch (NoSuchElementException e) {
      logger.debug("NoSuchElementException");
    }
    return found;
  }
  /**
   * return list of elements if list is present based on
   * locators type and locator
   * @param locatorTypeAndLocatorValue xpath expression of elements to be searched on UI.
   * @return true if elements are present false otherwise
   */
  public static List<WebElement> getListOfElements(By locatorTypeAndLocatorValue) {
    return getListOfElements(locatorTypeAndLocatorValue, Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
  }
  /**
   * return list of elements if list is present based on
   * locators type and locator
   * @param locatorTypeAndLocatorValue xpath expression of elements to be searched on UI.
   * @return true if elements are present false otherwise
   */
  public static List<WebElement> getListOfElements(By locatorTypeAndLocatorValue, Duration timeout) {
    List<WebElement> list = null;
    try {
      WebDriver driver = getWebDriverInstance();
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver,timeout);
      list = wait.until(x -> x.findElements(locatorTypeAndLocatorValue));

      if (list == null || list.isEmpty()) {
        logger.debug("NoSuchElementException");
      } else {
        logger.info("Members found by: "+locatorTypeAndLocatorValue+ "are: "+list);
      }
    } catch (TimeoutException e) {
      logger.debug("TimeoutException");
    } catch (NoSuchElementException e) {
      logger.debug("NoSuchElementException");
    }
    return list;
  }
  /*Todo Experimental*/
  /**
   * Checks for presence of element with specified xpath on UI. Driver doesn't
   * wait for expected element on screen.
   *
   * @param xpath
   *            -xpath expression of element to be searched on UI.
   * @return true if element is present false otherwise
   */
  public static boolean isElementPresent(String xpath) {
    boolean found = false;
    try {
      WebDriver driver = getWebDriverInstance();
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

      WebDriverWait wait = new WebDriverWait(driver,
          Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
      List<WebElement> elements = wait.until(x -> x.findElements(By.xpath(xpath)));

      // List<WebElement> elements = driver.findElements(By.xpath(xpath));
      if (!elements.isEmpty()) {
        found = true;
      }
    } catch (NoSuchElementException e) {
      logger.debug("NoSuchElementException");
    } catch (TimeoutException e) {
      logger.debug("TimeoutException");
    }
    return found;
  }



  /*Todo Experimental*/
  /**
   * This method for parallel execution
   * Checks for presence of element with specified xpath on UI. Driver doesn't
   * wait for expected element on screen.
   *
   * @param xpath
   *            -xpath expression of element to be searched on UI.
   * @return true if element is present false otherwise
   */
  public static boolean isElementPresent(String xpath,WebDriver driverParallel) {
    boolean found = false;
    try {
      WebDriver driver = driverParallel;
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

      WebDriverWait wait = new WebDriverWait(driver,
          Duration.ofSeconds(Integer.parseInt(configProps.getProperty("maxTimeout"))));
      List<WebElement> elements = wait.until(x -> x.findElements(By.xpath(xpath)));

      // List<WebElement> elements = driver.findElements(By.xpath(xpath));
      if (!elements.isEmpty()) {
        found = true;
      }
    } catch (NoSuchElementException e) {
      logger.error("NoSuchElementException Xpath - "+xpath,e);
      return false;
    } catch (TimeoutException e) {
      logger.error("TimeoutException Xpath - "+xpath,e);
      return false;
    }
    catch (Exception e) {
      logger.error("Unexpected Exception --Xpath - "+xpath,e);
      return false;
    }
    return found;
  }

  /**
   * Checks for presence of element with specified xpath. It will search for
   * element and waits for it until specified timeout.
   *
   * @param xpath xpath expression of element to be searched on UI.
   * @param timeout timeout to wait for element
   * @return true if element is present false otherwise
   */
  public static boolean isElementPresent(String xpath, Duration timeout) {
    boolean present = false;
    try {
      WebDriver driver = getWebDriverInstance();
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      List<WebElement> elements = wait.until(x -> x.findElements(By.xpath(xpath)));
      if (!elements.isEmpty()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }




  /**
   *   This method is for parallel execution
   *  Checks for presence of element with specified xpath. It will search for
   * element and waits for it until specified timeout.
   *
   * @param xpath xpath expression of element to be searched on UI.
   * @param timeout timeout to wait for element
   * @return true if element is present false otherwise
   */
  public static boolean isElementPresent(String xpath, Duration timeout,WebDriver driverParallel) {
    boolean present = false;
    try {
      WebDriver driver = driverParallel;
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      List<WebElement> elements = wait.until(x -> x.findElements(By.xpath(xpath)));
      if (!elements.isEmpty()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }


  public static boolean isElementPresent(String xpath, int timeout,WebDriver driverParallel) {
    boolean present = false;
    try {
      WebDriver driver = driverParallel;
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
      List<WebElement> elements = wait.until(ExpectedConditions
              .presenceOfAllElementsLocatedBy(By.xpath(xpath)));
      if (!elements.isEmpty()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }


  public static boolean isElementPresent(WebElement we, int timeout, WebDriver driver) {
    boolean present = false;
    try {
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
      WebElement element = wait.until(ExpectedConditions
              .visibilityOf(we));
      if (element!=null) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }

  /**
   * Checks for presence of element with specified xpath. It will search for
   * element and waits for it until specified timeout.
   *
   * @param className classname to wait for element
   * @param timeout timeout to wait for element
   * @return true if element is present false otherwise
   */
  public static boolean isElementPresentByClassName(String className, Duration timeout) {
    boolean present = false;
    try {
      WebDriver driver = getWebDriverInstance();
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      List<WebElement> elements = wait.until(x -> x.findElements(By.className(className)));
      if (!elements.isEmpty()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }


  /**
   * This method is for parallel processing
   * Checks for presence of element with specified xpath. It will search for
   *  element and waits for it until specified timeout.
   *
   * @param className
   * @param timeout
   * @param driverParallel
   * @return
   */

  public static boolean isElementPresentByClassName(String className, Duration timeout,WebDriver driverParallel) {
    boolean present = false;
    try {
      WebDriver driver = driverParallel;
      // nullify implicitlyWait
      driverParallel.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      List<WebElement> elements = wait.until(x -> x.findElements(By.className(className)));
      if (!elements.isEmpty()) {
        present = true;
      }
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }

  /**
   * Checks for presence of List of elements with specified xpath on UI.
   * Driver doesn't wait for expected elements on screen.
   *
   * @param xpath xpath expression of elements to be searched on UI.
   * @return true if elements are present false otherwise
   */
  public static boolean isListOfElementsPresent(String xpath) {
    boolean found = false;
    try {
      WebDriver driver = getWebDriverInstance();
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver,
          Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
      List<WebElement> list = wait.until(x -> x.findElements(By.xpath(xpath)));

      if (list == null || list.isEmpty()) {
        found = false;
      } else {
        found = true;
      }
    } catch (TimeoutException e) {
      logger.debug("TimeoutException");
    } catch (NoSuchElementException e) {
      logger.debug("NoSuchElementException");
    }
    return found;
  }


  /**
   * Checks for presence of List of elements with specified xpath on UI.
   * Driver doesn't wait for expected elements on screen.
   * this method for parallel processing
   * @param xpath xpath expression of elements to be searched on UI.
   * @return true if elements are present false otherwise
   */
  public static boolean isListOfElementsPresent(String xpath,WebDriver driverParallel) {
    boolean found = false;
    try {
      WebDriver driver = driverParallel;
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver,
          Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
      List<WebElement> list = wait.until(x -> x.findElements(By.xpath(xpath)));

      if (list == null || list.isEmpty()) {
        found = false;
      } else {
        found = true;
      }
    } catch (TimeoutException e) {
      logger.debug("TimeoutException");
    } catch (NoSuchElementException e) {
      logger.debug("NoSuchElementException");
    }catch (Exception e)
    {
      logger.error("Unexpected exception at isListofElementsPresent",e);
    }
    return found;
  }

  /** Checks to see that the position of a particular web element is not changing.
   * If the element is not changing for at least 10 loops, we assume it's stable.
   * We cap the max number of times to check that the element is stable to avoid looping infinitely.
   */
  public void waitForElementPositionToBeStable(WebElement we) {
    int maxChecks = 500;
    int maxStableCount = 10;
    int lastX = -1;
    int lastY = -1;
    int stableCount = 0;
    for (int i = 0; i < maxChecks && stableCount < maxStableCount; i++) {
      Point p = we.getLocation();
      logger.debug("Current coordinates: {}, {}", p.getX(), + p.getY());
      if (lastX == p.getX()) {
        if (lastY == p.getY()) {
          stableCount++;
        } else {
          lastY = p.getY();
          stableCount = 0;
        }
      } else {
        lastX = p.getX();
        stableCount = 0;
      }
      WaitTool.sleepShort(100);
    }
  }

  public void moveElementwithXY(WebElement element,int x,int y,WebDriver driverParallel)
  {
    Actions actions=new Actions(driverParallel);

    actions.clickAndHold(element).moveByOffset(x,y).build().perform();
  }

  public String returnWebElementXY(WebElement element)
  {
    return element.getLocation().getX() +"," + element.getLocation().getY();
  }


  /**
   * Checks for presence of elements with specified xpath. It will search for
   * elements and waits for them until specified timeout.
   *
   * @param xpath xpath expression of elements to be searched on UI.
   * @param timeout timeout to wait for element
   * @return True if elements were present and false otherwise
   */
  public static boolean isListOfElementsPresent(String xpath, Duration timeout) {
    boolean present = false;
    try {
      WebDriver driver = getWebDriverInstance();
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      wait.until(x -> x.findElements(By.xpath(xpath)));
      present = true;
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }

  /**
   * this method for parallel processing
   * Checks for presence of elements with specified xpath. It will search for
   * elements and waits for them until specified timeout.
   *
   * @param xpath xpath expression of elements to be searched on UI.
   * @param timeout timeout to wait for element
   * @return True if elements were present and false otherwise
   */
  public static boolean isListOfElementsPresent(String xpath, Duration timeout,WebDriver driverParallel) {
    boolean present = false;
    try {
      WebDriver driver = driverParallel;
      // nullify implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
      WebDriverWait wait = new WebDriverWait(driver, timeout);
      wait.until(x -> x.findElements(By.xpath(xpath)));
      present = true;
    } catch (Exception e) {
      logger.debug(e.getStackTrace());
    }
    return present;
  }

  /** Waits for the text to appear in the title.
   * Will wait as long as the configured avgTimeout value.
   * @param titleText partial text to wait for
   */
  public static void waitUntilTextInPageTitlePresent(String titleText,WebDriver driver) {
    waitUntilTextInPageTitlePresent(titleText,
            Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))),driver);
  }


  /** Waits for the text to appear in the title.
   *
   * @param titleText partial text to wait for
   * @param timeout time to wait in seconds
   */
  public static void waitUntilTextInPageTitlePresent(String titleText, Duration timeout) {
    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(), timeout);
    wait.until(x -> ExpectedConditions.titleContains(titleText));
  }

  /** Waits for the text to appear in the title.
   *
   * @param titleText partial text to wait for
   * @param timeout time to wait in seconds
   */
  public static void waitUntilTextInPageTitlePresent(String titleText, Duration timeout,WebDriver driver) {
    WebDriverWait wait = new WebDriverWait(driver, timeout);
    wait.until(x -> ExpectedConditions.titleContains(titleText));
  }

  /** Waits for the element to be clickable.
   * Will wait as long as the configured avgTimeout value.
   * @param element WebElement to wait for
   */
  public static void waitForElementClickable(WebElement element) {
    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(),
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(x -> ExpectedConditions.elementToBeClickable(element));
  }

  /** Waits for the element to be clickable. And this is method is for parallel execution
   * Will wait as long as the configured avgTimeout value.
   * @param element WebElement to wait for
   */
  public static void waitForElementClickable(WebElement element,WebDriver driverParallel) {
    WebDriverWait wait = (WebDriverWait) new WebDriverWait(driverParallel,
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout")))).ignoring(StaleElementReferenceException.class);
    wait.until( ExpectedConditions.elementToBeClickable(element));
  }

  public static void waitForElementClickableMin(WebElement element,WebDriver driverParallel) {
    WebDriverWait wait = new WebDriverWait(driverParallel,
            Duration.ofSeconds(Integer.parseInt(configProps.getProperty("minTimeout"))));
    wait.until(ExpectedConditions.elementToBeClickable(element));
  }

  /** Waits for the element to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param element WebElement to wait for
   */
  public static void waitForInvisibilityOfElement(WebElement element) {
    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(),
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(x -> ExpectedConditions.invisibilityOf(element));
  }


  /** Waits for the element to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param element WebElement to wait for
   */
  public static void waitForInvisibilityOfElement(WebElement element,WebDriver driverParallel) {
    WebDriverWait wait = (WebDriverWait) new WebDriverWait(driverParallel,
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout")))).ignoring(StaleElementReferenceException.class);
    wait.until( ExpectedConditions.invisibilityOf(element));
  }


  /** Waits for the element to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param element WebElement to wait for
   * @param waitTime Number of seconds to wait
   */
  public static void waitForInvisibilityOfElement(WebElement element, long waitTime) {
    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(), Duration.ofSeconds(waitTime));
    wait.until(x -> ExpectedConditions.invisibilityOf(element));
  }


  /** Waits for the element to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param element WebElement to wait for
   * @param waitTime Number of seconds to wait
   */
  public static void waitForInvisibilityOfElement(WebElement element, long waitTime,WebDriver driverParallel) {
    WebDriverWait wait = new WebDriverWait(driverParallel, Duration.ofSeconds(waitTime));
    int tries=0;
    int maxTries=Integer.parseInt(configProps.getProperty("maxRetries"));
    while(tries<maxTries) {
      try {
        wait.until(ExpectedConditions.invisibilityOf(element));
        break;
      } catch (Exception e) {
        logger.warn("Still element exists retry for invisibility -- "+tries);
        tries++;
      }
    }
  }

  /** Waits for the element identified by xPath to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param xPath WebElement to wait for
   */
  public static void waitForInvisibilityOfElement(String xPath) {

    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(),
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(x -> ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));
  }


  /**
   * this method for parallel processing
   * Waits for the element identified by xPath to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param xPath WebElement to wait for
   */
  public static void waitForInvisibilityOfElement(String xPath,WebDriver driverParallel) {

    WebDriverWait wait = new WebDriverWait(driverParallel,
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));
  }

  /** Waits for the element identified by xPath to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param xPath WebElement to wait for
   * @param waitTime number of seconds to wait
   */
  public static void waitForInvisibilityOfElement(String xPath, long waitTime) {
    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(), Duration.ofSeconds(waitTime));
    wait.until(x -> ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));
  }

  /** Waits for the element identified by xPath to be invisible.
   * Will wait as long as the configured maxTimeout value.
   * @param xPath WebElement to wait for
   */
  public static void waitForInvisibilityOfElementMax(String xPath) {
    WebDriver driver = getWebDriverInstance();
    // nullify implicitlyWait
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    WebDriverWait wait = new WebDriverWait(driver,
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("maxTimeout"))));
    wait.until(x -> ExpectedConditions.invisibilityOfElementLocated(By.xpath(xPath)));
  }

  /** Waits for the element to be invisible.
   * Will wait as long as the configured maxTimeout value.
   */
  public void waitForInvisibilityOfElementMax() {
    WebDriver driver = getWebDriverInstance();
    // nullify implicitlyWait
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    WebDriverWait wait = new WebDriverWait(driver,
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("maxTimeout"))));
    wait.until(x -> ExpectedConditions.invisibilityOfElementLocated(By.xpath(this.getXPathLocator())));
  }

  /** Waits for all the elements to be invisible.
   * Will wait as long as the configured avgTimeout value.
   * @param elementList WebElement list to wait for
   */
  public static void waitForInvisibilityOfAllElements(List<WebElement> elementList) {
    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(),
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(x -> ExpectedConditions.invisibilityOfAllElements(elementList));
  }

  /** Waits for the element to be visible.
   * Will wait as long as the configured avgTimeout value.
   * @param element WebElement to wait for
   */
  public static void waitForVisibilityOfElement(WebElement element) {
    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(),
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(x -> ExpectedConditions.visibilityOf(element));
  }

  /**
   *  This method is for parallel processing
   *  Waits for the element to be visible.
   * Will wait as long as the configured avgTimeout value.
   * @param element
   * @param driverParallel
   */
  public static void waitForVisibilityOfElement(WebElement element,WebDriver driverParallel) {
    WebDriverWait wait = new WebDriverWait(driverParallel,
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(ExpectedConditions.visibilityOf(element));
  }

  /** Waits for the element identified by xPath to be visible.
   * Will wait as long as the configured avgTimeout value.
   * @param xPath WebElement to wait for
   */
  public static void waitForVisibilityOfElement(String xPath) {

    WebDriverWait wait = new WebDriverWait(getWebDriverInstance(),
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
  }

  /**
   * this method for parallel processing
   * Waits for the element identified by xPath to be visible.
   * Will wait as long as the configured avgTimeout value.
   * @param xPath WebElement to wait for
   */
  public static Boolean waitForVisibilityOfElement(String xPath,WebDriver driverParallel) {

    WebDriverWait wait = new WebDriverWait(driverParallel,
        Duration.ofSeconds(Integer.parseInt(configProps.getProperty("avgTimeout"))));
    wait.until( ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
    return driverParallel.findElement(By.xpath(xPath)).isDisplayed();
  }

  /** Execute Javascript on the existing web element.
   *
   * @param script Javascript to execute
   * @return return object
   */
  public Object executeJavascript(String script) {
    JavascriptExecutor js = (JavascriptExecutor) getWebDriverInstance();
    return js.executeScript(script, getElementByXpath(getXPathLocator()));
  }

  /** Execute Javascript on the a web element parameter.
   *
   * @param we WebElement on which to execute js
   * @param script Javascript to execute
   * @return return object
   */
  public Object executeJavascript(String script, WebElement we) {
    JavascriptExecutor js = (JavascriptExecutor) getWebDriverInstance();
    return js.executeScript(script, we);
  }


  /** Execute Javascript on the a web element parameter.
   *
   * @param we WebElement on which to execute js
   * @param script Javascript to execute
   * @return return object
   */
  public Object executeJavascript(String script, WebElement we, WebDriver driverParallel) {
    JavascriptExecutor js = (JavascriptExecutor) driverParallel;
    return js.executeScript(script, we);
  }

  /** Hover on a element identified by Xpath such as to produce some effect like a tooltip popup.
   *
   * @param xPath Xpath of element to hover upon
   * @param delay Amount of delay to wait for the tooltip (in milliseconds)
   */
  public void hoverOnElement(String xPath, long delay) {
    new Actions(getWebDriverInstance()).moveToElement(getElementByXpath(xPath))
            .pause(delay).perform();
  }

  /** Hover on a element identified by Xpath such as to produce some effect like a tooltip popup.
   * this method for parallel processing
   * @param xPath Xpath of element to hover upon
   * @param delay Amount of delay to wait for the tooltip (in milliseconds)
   */
  public void hoverOnElement(String xPath, long delay,WebDriver driverParallel) {
    new Actions(driverParallel).moveToElement(getElementByXpath(xPath,driverParallel))
            .pause(delay).perform();
  }

  /** Hover on a web element such as to produce some effect like a tooltip popup.
   *
   * @param element Web element to hover upon
   * @param delay Amount of delay to wait for the tooltip (in milliseconds)
   */
  public void hoverOnElement(WebElement element, long delay) {
    new Actions(getWebDriverInstance()).moveToElement(element).pause(delay).perform();
  }

  /**
   * this method is for parallel processing
   * Hover on a web element such as to produce some effect like a tooltip popup.
   *
   * @param element Web element to hover upon
   * @param delay Amount of delay to wait for the tooltip (in milliseconds)
   */
  public void hoverOnElement(WebElement element, long delay,WebDriver driverParallel) {
    new Actions(driverParallel).moveToElement(element).pause(delay).perform();
  }

  public void hoverOnElementAndClick(WebElement element,WebDriver driverParallel) {
    new Actions(driverParallel).moveToElement(element).click().build().perform();
  }

  /** Hover on the defined web element.
   *
   * @param delay Amount of delay to wait for the tooltip (in milliseconds)
   */
  public void hoverOnElement(long delay) {
    new Actions(getWebDriverInstance()).moveToElement(getElementByXpath(getXPathLocator()))
            .pause(delay).perform();
  }

  /** Hover on a web element such as to produce some effect like a tooltip popup.
   * This will hovers with a delay of 3 seconds.
   * @param element Web element to hover upon
   */
  public void hoverOnElement(WebElement element) {
    hoverOnElement(element, 3000);
  }

  /**
   * this method is for parallel processing
   * Hover on a web element such as to produce some effect like a tooltip popup.
   * This will hovers with a delay of 3 seconds.
   * @param element Web element to hover upon
   */
  public void hoverOnElement(WebElement element,WebDriver driverParallel) {
    hoverOnElement(element, 3000,driverParallel);
  }
  public void hoverOnElementClick(WebElement element,WebDriver driverParallel) {
    hoverOnElementAndClick(element ,driverParallel);
  }

  /** Scroll expose the web element via Javascript.
   * @param we WebElement to try to scroll into view
   */
  public void scrollIntoView(WebElement we) {
    this.executeJavascript("arguments[0].scrollIntoView(true);", we);
  }

  /**
   * this method for parallel processing
   * Scroll expose the web element via Javascript.
   * @param we WebElement to try to scroll into view
   */
  public void scrollIntoView(WebElement we,WebDriver driverParallel) {
    try {
      this.executeJavascript("arguments[0].scrollIntoView(true);", we,driverParallel);
    } catch (Exception e) {
      logger.error("not able to scroll to view unexpected exception ",e);
    }
  }

  /**
   * this method for parallel processing
   * Scroll expose the web element via Javascript.
   * @param we WebElement to try to scroll into view
   */
  public Boolean scrollIntoViewHandle(WebElement we,WebDriver driverParallel) {
    try {
      this.executeJavascript("arguments[0].scrollIntoView(true);", we,driverParallel);
      return Boolean.TRUE;
    }
    catch (Exception e)
    {
      logger.warn("Unable to scroll ",e);
      return Boolean.FALSE;
    }
  }

  /** Scroll expose the web element via Javascript.
   * @param we WebElement to try to scroll into view
   * @param topOrBottom Control how to scroll the item into view:<br>
   * <li>TRUE = the top of the element will be aligned to the top of the visible area of
   *      the scrollable ancestor.
   * <li>FALSE = the bottom of the element will be aligned to the bottom of the visible area
   *      of the scrollable ancestor.
   */
  public void scrollIntoView(WebElement we, boolean topOrBottom) {
    if (topOrBottom) {
      this.executeJavascript("arguments[0].scrollIntoView(true);", we);
    } else {
      this.executeJavascript("arguments[0].scrollIntoView(false);", we);
    }
  }

  public Boolean isDataTableHasFragementTag(WebElement div)
  {
    if(Optional.ofNullable(div.findElement(By.tagName("span")).getAttribute("fragment")).isPresent())
      return div.findElement(By.tagName("span")).getAttribute("fragment").length() > 0;
    else
      return Boolean.FALSE;
  }



  /** Scroll expose the web element via Javascript.
   * this method for parallel processing
   * @param we WebElement to try to scroll into view
   * @param topOrBottom Control how to scroll the item into view:<br>
   * <li>TRUE = the top of the element will be aligned to the top of the visible area of
   *      the scrollable ancestor.
   * <li>FALSE = the bottom of the element will be aligned to the bottom of the visible area
   *      of the scrollable ancestor.
   */
  public void scrollIntoView(WebElement we, boolean topOrBottom, WebDriver driverParallel) {
    if (topOrBottom) {
      executeJavascript("arguments[0].scrollIntoView(true);", we, driverParallel);
    } else {
      executeJavascript("arguments[0].scrollIntoView(false);", we, driverParallel);
    }
  }


  /**
   * Get the list of WebElements matching the given user defined params.
   *
   * @param locatorTypeAndLocatorValue Xpath to use to find elements.
   * @param timeout                    number of seconds to wait before timing out
   * @return list of web elements
   */
  public WebElement getElement(By locatorTypeAndLocatorValue, int timeout, WebDriver driverParallel) {
    // nullify implicitlyWait
    driverParallel.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    WebDriverWait wait = new WebDriverWait(driverParallel, Duration.ofSeconds(timeout));
    WebElement element = wait.until(x -> x.findElement(locatorTypeAndLocatorValue));
    Assert.assertTrue(element != null && element.isEnabled());
    return element;
  }


  /**
   * Get the list of WebElements matching the given xpath using the configured minimum timeout.
   *
   * @param locatorTypeAndLocatorValue Xpath to use to find elements.
   * @return list of web elements
   */
  public WebElement getElement(By locatorTypeAndLocatorValue, WebDriver driverParallel) {
    return getElement(locatorTypeAndLocatorValue, Integer.parseInt(configProps.getProperty("minTimeout")), driverParallel);
  }

  public  Boolean performClickForInterceptedException(WebElement webElement, WebDriver driverParallel)
  {
    int maxTries=2;
    while (maxTries>0 && webElement.isDisplayed()) {
      try {
        webElement.click();
        return Boolean.TRUE;

      } catch (StaleElementReferenceException | ElementClickInterceptedException ee) {
        if(maxTries%2==0)
          new Actions(driverParallel).moveToElement(webElement).click().build().perform();
        else
          executeJavascript(Constants.JAVASCRIPT_CLICK_ACTION,webElement);
        maxTries--;

      } catch (Exception e) {
        logger.error("Unexpected exception");
        return Boolean.FALSE;
      }

    }

    return Boolean.FALSE;
  }

  public String getTextValue(WebElement ele ){
    String text;
    try {
      text= ele.getText();
      if (text.length()==0){
        text= ele.getAttribute("innerHTML").trim();
      }
    } catch (StaleElementReferenceException | ElementNotInteractableException ex)
    {
      text= this.executeJavascript(Constants.JAVASCRIPT_GETTEXT_ACTION,ele).toString();
    }
    catch (Exception e){
      logger.error("Failed to get text for the WebElement");
      throw e;
    }
    return text;
  }
  public static Boolean elementClick(WebElement element,WebDriver driverParallel)
  {
    try {
      element.click();
      return Boolean.TRUE;
    }
    catch (StaleElementReferenceException | ElementClickInterceptedException ee) {
      new Actions(driverParallel).moveToElement(element).click().build().perform();
      return Boolean.TRUE;
    }
    catch (Exception e)
    {
      logger.error("Unexpected exception at elementClick",e);
      return Boolean.FALSE;
    }
  }
  public String getTextValue(WebElement ele ,WebDriver driverParallel){
    String text;
    try {
      text= ele.getText();
      if (text.length()==0){
        text= ele.getAttribute("innerHTML").trim();
      }
    } catch (StaleElementReferenceException | ElementNotInteractableException ex)
    {
      text= this.executeJavascript(Constants.JAVASCRIPT_GETTEXT_ACTION,ele, driverParallel).toString();
    }
    catch (Exception e){
      logger.error("Failed to get text for the WebElement");
      throw e;
    }
    return text;
  }
}

