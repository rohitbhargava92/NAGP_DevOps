package org.nagp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>Wait tool class.  Provides Wait methods for an elements, and AJAX elements to load.
 * It uses WebDriverWait (explicit wait) for waiting an element or javaScript.</p>
 * <p>To use implicitlyWait() and WebDriverWait() in the same test,
 * we would have to nullify implicitlyWait() before calling WebDriverWait(), 
 * and reset after it.  This class takes care of it.</p>
 *
 * <p>Generally relying on implicitlyWait slows things down
 * so use the explicit wait methods of WaitTool as much as possible.
 * Also, consider (DEFAULT_WAIT_4_PAGE = 0) for not using implicitlyWait 
 * for a certain test.</p>
 *
 *
 */
public class WaitTool {
  private static final Logger log = LogManager.getLogger(WaitTool.class);
  /** Default wait time for an element. 7  seconds. */
  public static final int DEFAULT_WAIT_4_ELEMENT = 7;
  /** Default wait time for a page to be displayed.  12 seconds.
   * The average webpage load time is 6 seconds in 2012.
   * Based on your tests, please set this value.
   * "0" will nullify implicitlyWait and speed up a test.
   */
  public static final int DEFAULT_WAIT_4_PAGE = 12;

  /**
   * Wait for the element to be present in the DOM, and displayed on the page.
   * And returns the first WebElement using the given method.
   *
   * @param driver The driver object to be used
   * @param by selector to find the element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   * @return WebElement the first WebElement using the given method, or null
   *      (if the timeout is reached)
   */
  public static WebElement waitForElement(WebDriver driver, final By by, int timeOutInSeconds) {
    WebElement element;
    try {
      //To use WebDriverWait(), we would have to nullify implicitlyWait().
      //Because implicitlyWait time also set "driver.findElement()" wait time.
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); //nullify implicitlyWait()

      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds));
      //element = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
      element = wait.until(x -> x.findElement(by));

      //reset implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(DEFAULT_WAIT_4_PAGE));
      return element; //return the element
    } catch (Exception e) {
      log.warn("Exception waiting for element.", e);
    }
    return null;
  }

  /**
   * Wait for the element to be present in the DOM, regardless of being displayed or not.
   * And returns the first WebElement using the given method.
   *
   * @param driver The driver object to be used
   * @param by selector to find the element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   * @return the first WebElement using the given method
   *      or null (if the timeout is reached)
   */
  public static WebElement waitForElementPresent(WebDriver driver, final By by,
      int timeOutInSeconds) {
    WebElement element = null;
    List<WebElement> elements;
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(DEFAULT_WAIT_4_PAGE));
    try {
      WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds)).ignoring(StaleElementReferenceException.class);
      elements = wait.until(x -> x.findElements(by));
      if (!elements.isEmpty()) {
        element = elements.get(0);
      }
      //reset implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(DEFAULT_WAIT_4_PAGE));
    } catch (Exception e) {
      log.error("Unable to load element -- ", e);
     // Screenshot.addScreenshotFileName(driver,"waitForElement_"+System.currentTimeMillis());
      return element;
    }

    return element;
  }

  /**
   * Wait for the element identified by Xpath to be clickable
   * using the given method.
   *
   * @param driver The driver object to be used
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   *
   * @return WebElement when it is clickable using the given method, or null
   *         (if the timeout is reached)
   */
  public static WebElement waitForElementClickable(WebDriver driver, final By by, int timeOutInSeconds)
  {
    WebElement element = null;
    try
    {
      WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds)).ignoring(StaleElementReferenceException.class);
      element = wait.until(ExpectedConditions.elementToBeClickable(by));
    }
    catch (Exception e) {
      log.error("Unable to load element -- ", e);
      // Screenshot.addScreenshotFileName(driver,"waitForElement_"+System.currentTimeMillis());
      return element;
    }
    return element;
  }

  /**
   * Wait for the element identified by Xpath to be present in the DOM,
   * regardless of being displayed or not. And returns the first WebElement
   * using the given method.
   *
   * @param driver The driver object to be used
   * @param locator XPath locator for an element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   *
   * @return WebElement the first WebElement using the given method, or null
   *         (if the timeout is reached)
   */
  public static WebElement waitForElementPresentByXpath(WebDriver driver, String locator,
      int timeOutInSeconds) {
    return waitForElementPresent(driver, By.xpath(locator), timeOutInSeconds);
  }

  /**
   * Wait for the element identified by Xpath to be present in the DOM,
   * regardless of being displayed or not. And returns the first WebElement
   * using the given method.
   *
   * @param driver The driver object to be used
   * @param locator XPath locator for an element
   *
   * @return WebElement the first WebElement using the given method, or null
   *         (if the timeout is reached)
   */
  public static WebElement waitForElementPresentByXpath(WebDriver driver, String locator) {
    return waitForElementPresent(driver, By.xpath(locator), DEFAULT_WAIT_4_ELEMENT);
  }
  /**
   * Wait for the element identified by Xpath to be present in the DOM,
   * checks the visibility of the element. And returns the first WebElement
   * using the given method.
   *
   * @param driver The driver object to be used
   * @param locator XPath locator for an element
   * @param TimeOutInSeconds TimeOut required for wait
   *
   * @return WebElement the first WebElement using the given method, or null
   *         (if the timeout is reached)
   */
  public static WebElement waitForElementVisibleByXpath(WebDriver driver, String locator, long TimeOutInSeconds)  {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TimeOutInSeconds));


    try {
      return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    } catch (Exception e) {
      return  null;
    }
  }
/**
   * Wait for the element identified by Xpath to be present in the DOM,
   * checks the visibility of the element. And returns the first WebElement
   * using the given method.
   *
   * @param driver The driver object to be used
   * @param element WebElement to wait for
   * @param TimeOutInSeconds TimeOut required for wait
   *
   * @return WebElement the first WebElement using the given method, or null
   *         (if the timeout is reached)
   */
  public static WebElement waitForElementVisibleByWebElement(WebDriver driver, WebElement element, long TimeOutInSeconds)  {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TimeOutInSeconds));


    try {

      return wait.until(ExpectedConditions.visibilityOf(element));
    } catch (Exception e) {
      return  null;
    }
  }


  /*Todo Starts*/
  /**
   * Wait for the element identified by CssSelector to be present in the DOM,
   * regardless of being displayed or not. And returns the first WebElement
   * using the given method.
   *
   * @param driver The driver object to be used
   * @param locator CssSelector locator for an element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   *
   * @return WebElement the first WebElement using the given method, or null
   *         (if the timeout is reached)
   */
  public static WebElement waitForElementPresentByCssSelector(WebDriver driver, String locator,
                                                        int timeOutInSeconds) {
    return waitForElementPresent(driver, By.cssSelector(locator), timeOutInSeconds);
  }

  /**
   * Wait for the element identified by CssSelector to be present in the DOM,
   * regardless of being displayed or not. And returns the first WebElement
   * using the given method.
   *
   * @param driver The driver object to be used
   * @param locator CssSelector locator for an element
   *
   * @return WebElement the first WebElement using the given method, or null
   *         (if the timeout is reached)
   */
  public static WebElement waitForElementPresentByCssSelector(WebDriver driver, String locator) {
    return waitForElementPresent(driver, By.cssSelector(locator), DEFAULT_WAIT_4_ELEMENT);
  }
  /*Todo end*/
  /** Wait for the List&lt;WebElement&gt; to be present in the DOM, regardless of
   * being displayed or not. Returns all elements within the current page DOM.
   *
   * @param driver The driver object to be used
   * @param by selector to find the element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   * @return List&lt;WebElement&gt; all elements within the current page DOM, or
   *         null (if the timeout is reached)
   */
  public static List<WebElement> waitForListElementsPresent(WebDriver driver, final By by,
      int timeOutInSeconds) {
    try {
      List<WebElement> elements;
      WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds)).ignoring(StaleElementReferenceException.class);
      elements = wait.until(x -> x.findElements(by));
      // reset implicitlyWait
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(DEFAULT_WAIT_4_PAGE));

      return elements;
    }catch (Exception e)
    {
      return null;
    }
  }

  /**
   * Wait for the List&lt;WebElement&gt; to be present in the DOM, regardless of
   * being displayed or not. Returns all elements within the current page DOM.
   *
   * @param driver The driver object to be used
   * @param locator XPath locator for an element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   * @return List&lt;WebElement&gt; all elements within the current page DOM, or
   *         null (if the timeout is reached)
   */
  public static List<WebElement> waitForListElementsPresentByXpath(WebDriver driver, String locator,
      int timeOutInSeconds) {
    return waitForListElementsPresent(driver, By.xpath(locator), timeOutInSeconds);
  }

  /**
   * Wait for the List&lt;WebElement&gt; to be present in the DOM, regardless of
   * being displayed or not. Returns all elements within the current page DOM.
   *
   * @param driver
   *            The driver object to be used
   * @param locator
   *            XPath locator for an element
   *
   * @return List&lt;WebElement&gt; all elements within the current page DOM, or
   *         null (if the default timeout is reached)
   */
  public static List<WebElement> waitForListElementsPresentByXpath(WebDriver driver,
      String locator) {
    return waitForListElementsPresent(driver, By.xpath(locator), DEFAULT_WAIT_4_ELEMENT);
  }

  /**
   * <p>Wait for an element to appear on the refreshed web-page.
   * And returns the first WebElement using the given method.</p>
   *
   * <p>This method is to deal with dynamic pages.</p>
   *
   * <p>Some sites I (Mark) have tested have required a page refresh to add additional elements
   * to the DOM. Generally you (Chon) wouldn't need to do this in a typical AJAX scenario.</p>
   *
   * @param driver The driver object to use to perform this element search
   * @param by selector to find the element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   * @return the first WebElement using the given method, or null(if the timeout is reached)
   *
   * @author Mark Collin
   */
  public static WebElement waitForElementRefresh(WebDriver driver, final By by,
      int timeOutInSeconds) {
    WebElement element;
    try {
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS); //nullify implicitlyWait()
      new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds)) {
      }.until(driverObject -> {
        driverObject.navigate().refresh(); //refresh the page ****************
        return isElementPresentAndDisplay(driverObject, by);
      });
      element = driver.findElement(by);
      //reset implicitlyWait
      driver.manage().timeouts().implicitlyWait(DEFAULT_WAIT_4_PAGE, TimeUnit.SECONDS);
      return element; //return the element
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Wait for the Text to be present in the given element, regardless of being displayed or not.
   *
   * @param driver The driver object to be used to wait and find the element
   * @param by selector of the given element, which should contain the text
   * @param text The text we are looking
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   *
   * @return boolean
   */
  public static boolean waitForTextPresent(WebDriver driver, final By by, final String text,
      int timeOutInSeconds) {
    boolean isPresent = false;
    try {
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS); //nullify implicitlyWait()
      new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds)) {
      }.until(driverObject -> {
        //is the Text in the DOM
        return isTextPresent(driverObject, by, text);
      });
      isPresent = isTextPresent(driver, by, text);
      //reset implicitlyWait
      driver.manage().timeouts().implicitlyWait(DEFAULT_WAIT_4_PAGE, TimeUnit.SECONDS);
      return isPresent;
    } catch (Exception e) {
      log.warn("Caught exception while waiting for text present.", e);
    }
    return false;
  }

  /**
   * Waits for the Condition of JavaScript.
   *
   * @param driver The driver object to be used to wait and find the element
   * @param javaScript The javaScript condition we are waiting. e.g.
   *      "return (xmlhttp.readyState >= 2 && xmlhttp.status == 200)"
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   *
   * @return true or false(condition fail, or if the timeout is reached)
   **/
  public static boolean waitForJavaScriptCondition(WebDriver driver, final String javaScript,
      int timeOutInSeconds) {
    boolean jscondition = false;
    try {
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS); //nullify implicitlyWait()
      new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds)) {
      }.until(
          (driverObject -> (Boolean) ((JavascriptExecutor) driverObject).executeScript(javaScript))
      );
      jscondition =  (Boolean) ((JavascriptExecutor) driver).executeScript(javaScript);
      //reset implicitlyWait
      driver.manage().timeouts().implicitlyWait(DEFAULT_WAIT_4_PAGE, TimeUnit.SECONDS);
      return jscondition;
    } catch (Exception e) {
      log.warn("Caught exception while waiting for javascript condition.", e);
    }
    return false;
  }

  /** Waits for the completion of Ajax jQuery processing by checking "return jQuery.active == 0"
   *  condition.
   *
   * @param driver The driver object to be used to wait and find the element
   * @param timeOutInSeconds The time in seconds to wait until returning a failure
   *
   * @return boolean true or false(condition fail, or if the timeout is reached)
   * */
  public static boolean waitForJQueryProcessing(WebDriver driver, int timeOutInSeconds) {
    boolean jqueryCondition = false;
    try {
      driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS); //nullify implicitlyWait()
      new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds)) {
      }.until(
          driverObject -> (Boolean) ((JavascriptExecutor) driverObject)
              .executeScript("return jQuery.active == 0"));
      jqueryCondition = (Boolean) ((JavascriptExecutor) driver)
          .executeScript("return jQuery.active == 0");
      driver.manage().timeouts()
          .implicitlyWait(DEFAULT_WAIT_4_PAGE, TimeUnit.SECONDS); //reset implicitlyWait
      return jqueryCondition;
    } catch (Exception e) {
      log.warn("Caught exception while waiting for JQuery processing.", e);
    }
    return jqueryCondition;
  }

  /**
   * Coming to implicit wait, If you have set it once then you would have to explicitly set it to
   * zero to nullify it.
   */
  public static void nullifyImplicitWait(WebDriver driver) {
    //nullify implicitlyWait()
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
  }

  /**
   * Set driver implicitlyWait() time.
   * @param driver web driver to reset
   * @param waitTimeInSeconds wait time in seconds
   */
  public static void setImplicitWait(WebDriver driver, int waitTimeInSeconds) {
    driver.manage().timeouts().implicitlyWait(waitTimeInSeconds, TimeUnit.SECONDS);
  }

  /**
   * Reset ImplicitWait.
   * To reset ImplicitWait time you would have to explicitly
   * set it to zero to nullify it before setting it with a new time value.
   * @param driver web driver to reset
   */
  public static void resetImplicitWait(WebDriver driver) {
    //nullify implicitlyWait()
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    //reset implicitlyWait
    driver.manage().timeouts().implicitlyWait(DEFAULT_WAIT_4_PAGE, TimeUnit.SECONDS);
  }

  /**
   * Reset ImplicitWait.
   * @param driver web driver
   * @param newWaitTimeInSeconds - a new wait time in seconds
   */
  public static void resetImplicitWait(WebDriver driver, int newWaitTimeInSeconds) {
    //nullify implicitlyWait()
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    //reset implicitlyWait
    driver.manage().timeouts().implicitlyWait(newWaitTimeInSeconds, TimeUnit.SECONDS);
  }

  /**
   * Checks if the text is present in the element.
   *
   * @param driver - The driver object to use to perform this element search
   * @param by - selector to find the element that should contain text
   * @param text - The Text element you are looking for
   * @return true or false
   */
  private static boolean isTextPresent(WebDriver driver, By by, String text) {
    try {
      return driver.findElement(by).getText().contains(text);
    } catch (NullPointerException e) {
      return false;
    }
  }

  /**
   * Checks if the element is in the DOM, regardless of being displayed or not.
   *
   * @param driver The driver object to use to perform this element search
   * @param by selector to find the element
   * @return boolean
   */
  private static boolean isElementPresent(WebDriver driver, By by) {
    /* When element not found it will throw NoSuchElementException, which calls "catch(Exception)"
     and returns false; */
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  /**
   * Checks if the List&lt;WebElement&gt; are in the DOM, regardless of being displayed or not.
   *
   * @param driver The driver object to use to perform this element search
   * @param by selector to find the element
   * @return boolean
   */
  private static boolean areElementsPresent(WebDriver driver, By by) {
    try {
      driver.findElements(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  /**
   * Checks if the element is in the DOM and displayed.
   *
   * @param driver The driver object to use to perform this element search
   * @param by selector to find the element
   * @return boolean
   */
  private static boolean isElementPresentAndDisplay(WebDriver driver, By by) {
    try {
      return driver.findElement(by).isDisplayed();
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  /**
   * Waits for page load (in theory).
   * @param driver The driver object to use to waitForPageLoad
   * @param seconds number of seconds
   */
  public void waitForPageLoad(WebDriver driver, int seconds) {
    driver.manage().timeouts().pageLoadTimeout(seconds, TimeUnit.SECONDS);
    driver.manage().timeouts().setScriptTimeout(seconds, TimeUnit.SECONDS);
  }

  /**
   * Causes the currently executing thread to sleep (temporarily cease execution)
   * for the specified number of seconds.
   *
   * @param seconds - Number of seconds to make cease thread to sleep.
   */
  public static void sleep(long seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      log.error("Exception thrown by sleep", e);
    }
  }

  /**
   * Causes the currently executing thread to sleep (temporarily cease execution) for the
   * specified number of milliseconds.
   *
   * @param milliseconds - Number of milliseconds to make cease thread to sleep.
   */
  public static void sleepShort(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      log.error("Exception thrown by sleep", e);
    }
  }

  public static void waitForPageToLoad(WebDriver driverParallel)
  {
    try {
      new WebDriverWait(driverParallel,  Duration.ofSeconds(Integer.parseInt("10"))).until((ExpectedCondition<Boolean>) wd ->
              ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
    } catch (Exception e) {

    }
  }

}


