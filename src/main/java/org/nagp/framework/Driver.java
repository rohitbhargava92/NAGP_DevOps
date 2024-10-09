
package org.nagp.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

/** This class is setup to control a master web driver instance.
 *  (We can use this with a grid configuration later).
 *
 */
public abstract class Driver {
    private static WebDriver driverInstance = null;
    protected Map<String, String> parameters = new HashMap<>();
    private static Logger logger = LogManager.getLogger(Driver.class);
    private String rootXPath = "";
    private static Helper helper = new Helper();
    protected static final Properties configProps = helper.readConfig();

    /**
     * Constructor class for taking parameters.
     * @param params
     */
    public Driver(String... params) {
        for (String parameter : params) {
            String name = parameter.substring(0, parameter.indexOf("="));
            String value = parameter.substring(parameter.indexOf('=') + 1);
            parameters.put(name, value);
        }
    }

    /** Get the web driver instance.
     *
     * @return web driver instance
     */
    public static WebDriver getWebDriverInstance() {
        return driverInstance;
    }

    /** Initializes the web driver with the desired ChromeOptions.
     *
     * @param options Chrome options to apply for the web driver.
     * @return WebDriver
     */
    public static WebDriver initializeWebDriver(ChromeOptions options) {
        // Need to explicitly initialize driver instance here.
        // DO NOT try to simplify this to only return driver.
        driverInstance = new ChromeDriver(options);
        return driverInstance;
    }

    /** Initializes the web driver with the firefox options.
     *
     * @param options Firefox options of the web driver.
     * @return WebDriver
     */
    public static WebDriver initializeWebDriver(FirefoxOptions options) {
        // Need to explicitly initialize driver instance here.
        // DO NOT try to simplify this to only return driver.
        driverInstance = new FirefoxDriver(options);
        return driverInstance;
    }

    /** Initializes the web driver with the safari options.
     *
     * @param options Safari options of the web driver.
     * @return WebDriver
     */
    public static WebDriver initializeWebDriver(SafariOptions options) {
        // Need to explicitly initialize driver instance here.
        // DO NOT try to simplify this to only return driver.
        driverInstance = new SafariDriver(options);
        return driverInstance;
    }

    /** Initializes the web driver with the edge options.
     *
     * @param options Edge options of the web driver.
     * @return WebDriver
     */
    public static WebDriver initializeWebDriver(EdgeOptions options) {
        // Need to explicitly initialize driver instance here.
        // DO NOT try to simplify this to only return driver.
        driverInstance = new EdgeDriver(options);
        return driverInstance;
    }

    /** Get the xpathlocator for the class.
     *
     * @return An xpath locator
     */
    public String getXPathLocator() {
        return rootXPath;
    }

    /** Get the config properties from the config.properties file.
     *
     * @return configuration properties.
     */
    public static Properties getConfigProperties() {
        return configProps;
    }


}
