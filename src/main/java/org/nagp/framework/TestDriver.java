package org.nagp.framework;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class TestDriver {
    private static Logger logger = LogManager.getLogger(TestDriver.class);

    private static final String BROWSER = "browser";

    private WebDriver webDriver = null;
    private Helper helper = new Helper();
    private Properties configProps = helper.readConfig();

    protected static ThreadLocal<RemoteWebDriver> driverTH = new ThreadLocal<>();

    enum Browser {
        FIREFOX, CHROME, SAFARI, EDGE
    }

    public WebDriver getWebDriver() {
        return this.webDriver;
    }

    public void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public void initialize() {
        System.clearProperty("hudson.model.DirectoryBrowserSupport.CSP");
        System.clearProperty("jenkins.model.DirectoryBrowserSupport.CSP");
        System.setProperty("webdriver.chrome.whitelistedIps","");
        System.setProperty("jenkins.model.DirectoryBrowserSupport.CSP", "sandbox allow-same-origin allow-scripts; default-src 'self' 'unsafe-inline' 'unsafe-eval'; img-src 'self' data:; font-src 'self' data:");
        System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "sandbox allow-same-origin allow-scripts; default-src 'self' 'unsafe-inline' 'unsafe-eval'; img-src 'self' data:; font-src 'self' data:");
        try {
            setupWebDriver();
        } catch (Exception e) {
            logger.error("Driver initialization failed.", e);
        }
    }

    synchronized private void setupWebDriver(){
        if (webDriver == null) {
            logger.debug("The webDriver object is null, so getting Driver Object from Driver Class.");
            if (configProps == null) {
                logger.error("Failed to initialize the configuration details.");
                return;
            }

            switch (setBrowser(configProps.getProperty(BROWSER).trim())) {
                case FIREFOX:
                    logger.info("Browser firefox is requested by user...");
                    FirefoxProfile profile = new FirefoxProfile();
                    profile.setPreference("browser.download.folderList", 2);
                    profile.setPreference("browser.download.dir", System.getProperty("java.io.tmpdir"));
                    profile.setPreference("browser.download.useDownloadDir", true);
                    profile.setPreference("browser.helperApps.neverAsk.openFile",
                            "text/csv,application/x-msexcel,application/excel,application/x-excel,"
                                    + "application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,"
                                    + "application/msword,application/xml,application/pdf");
                    profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                            "text/csv,application/x-msexcel,application/excel,application/x-excel,"
                                    + "application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,"
                                    + "application/msword,application/xml,application/pdf");
                    profile.setPreference("pdfjs.disabled", true);  // disable the built-in PDF viewer

                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (Boolean.parseBoolean(configProps.getProperty("browserHeadless"))) {
                        logger.info("Browser firefox requested as headless mode.");
                        // firefoxOptions.setHeadless(true);
                    }
                    firefoxOptions.setProfile(profile);
                    firefoxOptions.addArguments("--window-size=1280x1024");

                    webDriver = Driver.initializeWebDriver(firefoxOptions);
                    break;
                case SAFARI:
                    logger.info("Browser safari is requested by user...");
                    SafariOptions safariOptions = new SafariOptions();
                    webDriver = Driver.initializeWebDriver(safariOptions);
                    break;
                case EDGE:
                    logger.info("Browser edge is requested by user...");
                    EdgeOptions edgeOptions = new EdgeOptions();
                    webDriver = Driver.initializeWebDriver(edgeOptions);
                    break;
                case CHROME:
                    logger.info("Browser chrome is requested by user...");
                    ChromeOptions options = new ChromeOptions();
                    DesiredCapabilities cap= new DesiredCapabilities();
                    cap.setCapability("resolution", "1920X1080");
                    HashMap<String, Object> chromePref = new HashMap<>();
                    chromePref.put("download.default_directory", System.getProperty("java.io.tmpdir"));
                    chromePref.put("profile.default_content_setting_values.automatic_downloads", 1);
                    options.setExperimentalOption("prefs", chromePref);

                    if(System.getProperty("os.name").toLowerCase().contains("window")){
                        options.setCapability(CapabilityType.PLATFORM_NAME, Platform.WINDOWS);
                    }

                    else if(System.getProperty("os.name").equalsIgnoreCase("Mac OS X"))
                    {
                        options.setCapability(CapabilityType.PLATFORM_NAME, Platform.MAC);
                    }
                    else
                        options.setCapability(CapabilityType.PLATFORM_NAME, Platform.LINUX);

                    options.addArguments("--window-size=1280x1024");
                    options.addArguments("disable-infobars");
                    options.setAcceptInsecureCerts(true);
                    options.addArguments("--no-sandbox");
                    options.addArguments("--whitelisted-ips");
                    options.addArguments("--disable-web-security");
                    options.addArguments("--remote-allow-origins=*");
                    options.addArguments("--ignore-certificate-errors");
                    options.addArguments("--disable-site-isolation-for-policy");
                    options.addArguments("--enable-javascript");
                    options.addArguments("--disable-dev-shm-usage");;
                    WebDriverManager.chromedriver().setup();
                    if (Boolean.parseBoolean(configProps.getProperty("browserHeadless"))) {
                        logger.info("############## -- Browser chrome requested as headless mode.----- ###########################");
                        options.addArguments("--headless");
                        options.addArguments("--no-sandbox");
                        options.addArguments("--disable-gpu");
                        options.merge(cap);
                        logger.info("############## -- EIQ Webdriver Initialization.----- ###########################");
                        driverTH.set(new ChromeDriver(options));
                        setWebDriver(driverTH.get());
                        setDefaultBrowserSize(webDriver);

                    }else {
                        options.merge(cap);
                        driverTH.set(new ChromeDriver(options));
                        setWebDriver(driverTH.get());
                        setDefaultBrowserSize(webDriver);
                    }
                    break;
                default:
                    logger.fatal("Unsupported browser type in configuration."
                            + " Please check for a supported value.");
            }
        }
    }

    private Browser setBrowser(String browser) {
        if (browser.equalsIgnoreCase("firefox")) {
            return Browser.FIREFOX;
        } else if (browser.equalsIgnoreCase("safari")) {
            return Browser.SAFARI;
        } else if (browser.equalsIgnoreCase("edge")) {
            return Browser.EDGE;
        } else {
            return Browser.CHROME;
        }
    }

    private void setDefaultBrowserSize(WebDriver wd) {
        wd.manage().window().setSize(new Dimension(1280, 1024));
        logger.info("Browser size = {}", webDriver.manage().window().getSize());
    }

    public List<LogEntry> getBrowserConsoleLogs(WebDriver driverParallel) {
        if (setBrowser(configProps.getProperty(BROWSER).trim()) == Browser.CHROME) {
            LogEntries log = driverParallel.manage().logs().get(BROWSER);
            return log.getAll();
        } else {
            logger.warn("Browser console logs are unavailable for this browser type.");
            return new ArrayList<>();
        }
    }
    public void setup() {
        logger.info("_________________________________________________________________");
        logger.info("Executing test case: ...");
        //Add for Screenshot

        if (Boolean.parseBoolean(configProps.getProperty("captureRecording"))) {
            int limit = Integer.parseInt(configProps.getProperty("captureRecLimit"));
        } else {
            logger.info("Recording is not enabled for this run based on config properties.");
        }
    }

    public void quitWebDriverForParallel(WebDriver driver) {
        logger.debug("shutting down driver reference and quitting application...");
        if (driver != null) {
            logger.debug("Quitting WebDriver instance");
            driver.quit();
        }
    }

}
