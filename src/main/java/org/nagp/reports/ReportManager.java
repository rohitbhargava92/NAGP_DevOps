package org.nagp.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.nagp.dataProvider.Constants;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportManager {

    private static String path;
    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null)
            createInstance();

        return extent;
    }


    public static ExtentReports createInstance() {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyymmddhhmmss");
        LocalDateTime now = LocalDateTime.now();
        if (extent == null) {
            String reportName = "NAGP_Selenium_Report"+dtf.format(now)+".html";

            if (System.getProperty("os.name").toLowerCase().contains("mac")
                    || System.getProperty("os.name").toLowerCase().contains("linux")) {
                path = "//target//" + reportName;
            } else {
                path = "CurrentTestResults" + File.separator+ reportName;
            }
            ExtentSparkReporter htmlReporter = new ExtentSparkReporter(Constants.UPLOAD_PATH + path);
            htmlReporter.config().setTheme(Theme.STANDARD);
            htmlReporter.config().setDocumentTitle("Report");
            htmlReporter.config().setEncoding("utf-8");
            htmlReporter.config().setReportName("Automated Tests - Report");
            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);
        }
        return extent;
    }
}
