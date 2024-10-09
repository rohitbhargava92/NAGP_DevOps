package org.nagp.dataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nagp.framework.Helper;

/**
 * This class is used to load XML test data for particular test cases.
 *
 * @author rohitBhargava
 *
 */
public class TestDataReader {
    private static final Logger LOGGER = LogManager.getLogger(TestDataReader.class);
    private static Helper helper = new Helper();
    private static Properties props = helper.readConfig();
    private static String testDataFileName = null;
    private static XmlParser xmlparser = null;

    /** Construct an instance of this class.
     * This is private to hide it from the usual constructed means.
     */
    private TestDataReader() {
    }

    /**
     * This method is for initializing the data reader class to
     * load data from the test data node in the configured Xml file.
     *
     * @apiNote TestDataReader.init() to be called before using test data.
     */
    public static void init() {
        LOGGER.info("Initializing TestDataReader...");
        testDataFileName = props.getProperty("testDataFile");
        xmlparser = new XmlParser(props.getProperty("host"));
    }

    /**
     * This method is for initializing the data reader class to
     * load data from the host node from the named test XML file.
     * @apiNote TestDataReader.init() to be called before using test data.
     *
     * @param fileName Name of XML file to read from
     */
    public static synchronized void init(String fileName) {
        LOGGER.info("Initializing TestDataReader...");
        testDataFileName = fileName;
        xmlparser = new XmlParser(props.getProperty("host"));
    }

    /**
     * This method is for obtaining data from specific test nodes. This will
     * return Map containing Name-Value pair where Name is TestNodeName and
     * value is TestDataValue
     *
     * @param tagXpath
     *            An xpath to the data elements
     * @return resultMap A Hashmap containing the desired values read from XML
     *         nodes
     */
    @SuppressWarnings("unchecked")
    synchronized public static Map<String, String> getDataMap(String tagXpath) {
        HashMap<String, String> resultMap = new HashMap<>();
        if (tagXpath.trim().isEmpty()) {
            throw new IllegalArgumentException("Xpath(Parameter) for data cannot be empty");
        } else {
            String[] mydata;

            // Parse nodes by using forward slash (preferred)
            if (tagXpath.contains("/")) {
                mydata = tagXpath.split("/");
            } else { // Parse by using back slashes
                mydata = tagXpath.split("'\'");
            }

            if (testDataFileName == null) {
                testDataFileName = props.getProperty("testDataFile");
            }

            LOGGER.debug("Attempting to build hashmap based on file '{}' and Node = {}",
                    testDataFileName, mydata[0]);

            HashMap<String, Object> test = (HashMap<String, Object>)
                    xmlparser.parseXml(testDataFileName, mydata[0]);
            if (test.isEmpty()) {
                throw new IllegalArgumentException("Failed to build the hashmap based on input XML path."
                        + " Please check the configuration.");
            } else if (mydata.length > 1) {
                resultMap = TestDataReader.loopToGetData(mydata, test, resultMap);
            } else {
                resultMap = (HashMap<String, String>) test.get(mydata[0]);
            }
        }
        return resultMap;
    }

    /** Loop through the parsed map to get all sub data nodes.
     *  This method is primarily added to reduce complexity the calling method.
     * @param data path to the data
     * @param parsedMap XML parsed data map
     * @param returnMap original data map to be returned
     * @return subset of data
     */
    private static HashMap<String, String> loopToGetData(String[] data, HashMap<String, Object> parsedMap,
                                                         HashMap<String, String> returnMap) {
        for (int i = 1; i < data.length; i++) {
            parsedMap = (HashMap<String, Object>) parsedMap.get(data[i - 1]);
            if (i == data.length - 1) {
                if (parsedMap.get(data[i]) == null || parsedMap.get(data[i]).toString().isEmpty()) {
                    throw new IllegalArgumentException("No Test Data Node found for the given XPath !");
                } else {
                    returnMap = (HashMap<String, String>) parsedMap.get(data[i]);
                }
            }
        }
        return returnMap;
    }

    /**
     * Return data values from map at a given xPath to a list of string. (This
     * is useful for getting a list of values without each user having to
     * convert on their own within a test case.)
     *
     * @param tagXpath
     *            An xpath to the data elements
     * @return a list of string containing the values
     * @author rohitBhargava
     *
     */
    public static List<String> getDataList(String tagXpath) {
        return new ArrayList<>(getDataMap(tagXpath).values());
    }

    /**
     * Look inside the XML file to determine whether the path exists or not.
     *
     * @param tagXpath
     *            An xpath to the data node you want to check
     * @return True if the path exists in the file and false if it does not
     *         exist.
     * @author rohitBhargava
     */
    public static boolean dataPathExists(String tagXpath) {
        boolean returnVal = false;
        try {
            TestDataReader.getDataList(tagXpath);
            returnVal = true;
        } catch (NullPointerException | IllegalArgumentException e) {
            // don't throw back any exception
        }
        return returnVal;
    }

    /** Returns the count of nodes from the path which start with a given name.
     *
     * @param tagXpath parent node name to look for items
     * @param name name of sub-node (partial name ok) to use to find a count
     * @return count of sub-nodes matching the names; 0 if none match.
     */
    public static int getNodesCountStartsWithName(String tagXpath, String name) {
        int count = 0;
        Map<String, String> map = getDataMap(tagXpath);
        for (String s : map.keySet()) {
            if (s.startsWith(name)) {
                count++;
            }
        }
        return count;
    }

}
