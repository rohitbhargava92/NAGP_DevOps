package org.nagp.listeners;

import org.nagp.framework.Helper;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Properties;

public class RetryAnalyzer implements IRetryAnalyzer {
    int initialCount=0;
    private Helper helper = new Helper();
    private final Properties configProps = helper.readConfig();
    int macCount=Integer.parseInt(configProps.getProperty("maxRetries"));

    public boolean retry(ITestResult result) {
        if(initialCount<macCount)
        {
            initialCount++;
            return true;
        }
        return false;
    }

}
