package org.nagp.listeners;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.dataProvider.Constants;
import org.nagp.reports.ReportManager;
import org.nagp.reports.ReportTestManager;
import org.nagp.utils.FileHelper;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);
    WebDriver driver;
    String filePath = Constants.UPLOAD_PATH + "CurrentTestResults" + File.separator;

    String targetFileName=null;


    private static String getTestMethodName(ITestResult iTestResult) {
        return iTestResult.getMethod().getConstructorOrMethod().getName();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.info("***** Error " + result.getName() + " test has failed *****");
        ITestContext context = result.getTestContext();
        driver = (WebDriver) result.getAttribute("webDriver");
        String testClassName = result.getInstanceName().trim();
        String testMethodName = result.getName().toString().trim();
        String errDesc = result.getThrowable().toString().split("expected")[0].split(":")[1].replaceAll(" ", "_");
        takeScreenShot(testMethodName, errDesc, driver);
        File image = new File(targetFileName);
        String absolutePath= image.getAbsolutePath();
        ReportTestManager.getTest().log(Status.FAIL, result.getThrowable());
        ReportTestManager.getTest().fail("Screenshot",
                MediaEntityBuilder.createScreenCaptureFromPath(absolutePath).build());
    }

    public void takeScreenShot(String methodName, String errDesc, WebDriver driver) {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyymmddhhmmss");
            LocalDateTime now = LocalDateTime.now();
            targetFileName=filePath + methodName + errDesc + dtf.format(now)+".png";
            FileUtils.copyFile(scrFile, new File(targetFileName));
            System.out.println("***Placed screen shot in " + filePath + " ***");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onFinish(ITestContext context) {
        System.out.println(("*** Test Suite " + context.getName() + " ending ***"));
        ReportManager.getInstance().flush();
    }

    public void onTestStart(ITestResult iTestResult) {
        System.out.println(iTestResult.getTestName());
        String description = iTestResult.getMethod().getDescription();
        if (iTestResult.getTestName() != null) {
            ReportTestManager.startTest(iTestResult.getTestName(),
                    iTestResult.getInstance().getClass().getCanonicalName());
        }else if (description != null)
            ReportTestManager.startTest(iTestResult.getMethod().getMethodName() + "( " + description + ")",
                    iTestResult.getInstance().getClass().getCanonicalName());
        else {
            ReportTestManager.startTest(iTestResult.getMethod().getMethodName(),
                    iTestResult.getInstance().getClass().getCanonicalName());
        }
    }

    public void onTestSuccess(ITestResult result) {
        ReportTestManager.getTest().log(Status.PASS, "Test passed");
    }

    public void onTestSkipped(ITestResult result) {
        ReportTestManager.getTest().log(Status.SKIP, "Test Skipped");
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        System.out.println("Test failed but it is in defined success ratio " + getTestMethodName(result));
    }


    @Override
    public void onStart(ITestContext context) {
        ITestListener.super.onStart(context);
        FileHelper fileHelper= new FileHelper();
        fileHelper.moveFiles();
        System.out.println("Moving files from Current Test Results to Archived Test Results");
    }
}
