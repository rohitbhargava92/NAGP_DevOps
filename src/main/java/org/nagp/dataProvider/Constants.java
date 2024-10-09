package org.nagp.dataProvider;

import java.io.File;

public class Constants {

    public static final String JAVASCRIPT_CLICK_ACTION = "arguments[0].click();";
    public static final String JAVASCRIPT_SCROLL_ACTION = "arguments[0].scrollIntoView(true);";

    public static final String JAVASCRIPT_GETTEXT_ACTION = "arguments[0].getAttribute('innerHTML');";
    public static final String UPLOAD_PATH = System.getProperty("user.dir") +File.separator + "src"+ File.separator + "main" + File.separator + "resources"+ File.separator;

    /**
     * Constructor to hide this from normal instantiation.
     */

    private Constants() {

    }
}
