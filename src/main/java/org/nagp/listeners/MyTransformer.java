package org.nagp.listeners;

import org.nagp.framework.Helper;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

public class MyTransformer implements IAnnotationTransformer {
    private Helper helper = new Helper();
    private final Properties configProps = helper.readConfig();

    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)
    {
        if(configProps.getProperty("retryTests").toLowerCase().contentEquals("true")){
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }

    }
}
