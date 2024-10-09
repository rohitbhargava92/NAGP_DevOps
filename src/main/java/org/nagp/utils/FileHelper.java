package org.nagp.utils;

import org.nagp.dataProvider.Constants;

import java.io.File;

public class FileHelper {

    File destinationFolder = new File(Constants.UPLOAD_PATH + "ArchivedTestResults" + File.separator);
    File sourceFolder = new File(Constants.UPLOAD_PATH + "CurrentTestResults" + File.separator);

    public void moveFiles() {
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        if (sourceFolder.exists() && sourceFolder.isDirectory()) {
            File[] listOfFiles = sourceFolder.listFiles();

            if (listOfFiles != null) {
                for (File child : listOfFiles) {
                    // Move files to destination folder
                    child.renameTo(new File(destinationFolder + "\\" + child.getName()));
                }
            }
        } else {
            System.out.println(sourceFolder + "  Folder does not exists");
        }
    }

}
