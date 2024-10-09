package org.nagp.framework;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class Helper {

    private static Logger logger = LogManager.getLogger(Helper.class);
    private Properties config;

    public Properties readConfig() {
        final String RESOURCE_NAME = "/config.properties";
        Properties props = new Properties();

        try {
            InputStream stream = getClass().getResourceAsStream(RESOURCE_NAME);
            props.load(stream);
        } catch (NullPointerException e) {
            logger.error("Resource {} does not exist: {}", RESOURCE_NAME, e.getMessage());
        } catch (IOException ioe) {
            logger.error("I/O Exception on loading {} - {}", RESOURCE_NAME, ioe.getMessage());
            throw new RuntimeException("Problem reading configuration!");
        }

        // Add the operating system as a property at run time.
        props.put("OS", opSysDetector());

        return props;
    }

    /**
     * Returns the config as properties.
     *
     * @return Properties
     */
    public Properties getConfig() {
        return config;
    }


    private String opSysDetector() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("nux") || os.contains("nix")) {
            return "linux";
        } else if (os.contains("mac")) {
            return "mac";
        } else {
            return "other";
        }
    }

    public int getCurrentDateOnly(){
        SimpleDateFormat dnt = new SimpleDateFormat("dd");
        Date date = new Date();
        return Integer.parseInt(dnt.format(date));
    }

    public int getDateOnlyTPlusMinus(int days){
        SimpleDateFormat dnt = new SimpleDateFormat("dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, days);
        return Integer.parseInt(dnt.format(c.getTime()));
    }

    public File getFile(final String filePath) {
        if (Files.exists(Paths.get(filePath))) {
            return new File(filePath);
        } else {
            //file does not exist in local path try getting it from Jar, extract the file name only
            // (assuming the file is at root location in jar)
            String jarFileName = StringUtils.substringAfterLast(filePath, "/");
            String jarFilePath = filePath.replace("src/main/resources/", "");
            String jarFileDirectory = jarFilePath.replace("/" + jarFileName, "");
            try {
                Files.createDirectory(Paths.get(jarFileDirectory));
            } catch (FileAlreadyExistsException e) {
                System.out.println(String.format("file : %s already exists: ignore the error", jarFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is = getClass().getClassLoader().getResourceAsStream(jarFilePath);
            if (is == null) {
                System.out.println("input stream is null");
            }
            System.out.println("getting file from jar :" + jarFilePath);
            File targetFile = new File(jarFilePath);
            try {
                Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (FileAlreadyExistsException e) {
                System.out.println(String.format("file : %s already exists: ignore the error", targetFile));
            } catch (IOException| NullPointerException e) {
                e.printStackTrace();
            }
            IOUtils.closeQuietly(is);
            return targetFile;
        }
    }

    public BigDecimal trimDecimalToXPlaces(Double number, Integer numberOfDecimalPlaces) {
        BigDecimal newNumber = BigDecimal.valueOf(number);
        BigDecimal newValue;
        if (number < 0) {
            newValue = newNumber.abs().setScale(numberOfDecimalPlaces, RoundingMode.FLOOR);
            newValue = newValue.multiply(BigDecimal.valueOf(-1));
        } else {
            newValue = newNumber.setScale(numberOfDecimalPlaces, RoundingMode.FLOOR);
        }

        return newValue;
    }

}
