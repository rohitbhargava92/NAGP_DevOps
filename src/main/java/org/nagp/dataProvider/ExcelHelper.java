package org.nagp.dataProvider;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nagp.framework.Helper;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExcelHelper {
  private static Logger logger = LogManager.getLogger(ExcelHelper.class);
  Helper helper = new Helper();
  public ExcelHelper() {

  }

  public Workbook getWorkbook(String filePath) {
    FileInputStream input;
    Workbook workbook = null;
    try {
      input = new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      throw new Error(String.format("Unable to open file path given %s", filePath));
    }

    try {
      if (filePath.endsWith("xlsx")) {
        workbook = new XSSFWorkbook(input);
      } else if (filePath.endsWith(".xls")) {
        workbook = new HSSFWorkbook(input);
      }
    } catch (IOException e) {
      throw new Error(String.format("Unable to retrieve workbook in filepath %s", filePath));
    }

    return workbook;
  }

  public Sheet getSheet(String filePath, Integer sheetNumber) {
    return getWorkbook(filePath).getSheetAt(sheetNumber);
  }

  public Sheet getSheet(String filePath, String sheetName) {
    return getWorkbook(filePath).getSheet(sheetName);
  }

  public List<String> readData(String ColumnName, String filePath, Integer sheetNumber) {

    File file = helper.getFile(filePath);
    Workbook workbook = getWorkbook(file.getPath());
    Sheet sheet = getSheet(file.getPath(), sheetNumber);
    int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
    int coloumnIndex = -1;
    String data = null;
    List<String> dataList = new ArrayList<>();
    for (int i = 0; i < rowCount + 1; i++) {

      Row row = sheet.getRow(i);
      if (coloumnIndex == -1) {
        for (int j = 0; j < row.getLastCellNum(); j++) {
          if (row.getCell(j).getStringCellValue() instanceof String && row.getCell(j)
              .getStringCellValue().equalsIgnoreCase(ColumnName)) {
            coloumnIndex = j;
            break;
          }
        }
      } else {
        try {
          data = row.getCell(coloumnIndex).getStringCellValue();
          dataList.add(data);
        } catch (Exception e) {
          Double a = (row.getCell(coloumnIndex).getNumericCellValue());
          BigDecimal newv = helper.trimDecimalToXPlaces(a, 0);
          dataList.add(String.valueOf(newv));
        }
      }
    }
    saveAndClose(workbook, filePath);
    return dataList;
  }

  public void saveAndClose(Workbook book, String filePath) {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(filePath);
      book.write(output);
      book.close();
    } catch (IOException e) {
      throw new Error(String.format("Unable to open, write or close file path given %s", filePath));
    }

  }


  public void updateExcel(String filePath, String sheetName, int rowNum, int cellNum,
      String newCellValue){
    try {
      FileInputStream fsIP = new FileInputStream(
          new File(filePath)); //Read the spreadsheet that needs to be updated
      final Workbook workbook = getWorkbook(filePath);
      final Sheet sheet = workbook.getSheet(sheetName);
      sheet.getRow(rowNum).getCell(cellNum).setCellValue(newCellValue);
      fsIP.close(); //Close the InputStream
      FileOutputStream output_file = new FileOutputStream(
          new File(filePath));  //Open FileOutputStream to write updates
      workbook.write(output_file); //write changes
      output_file.close();
    }
    catch (IOException e) {
      logger.warn("IOException on updateExcel method",e);
      e.printStackTrace();
    }
    catch (Exception e) {
      logger.error("Unexpected exception on updateExcel method",e);
    }
  }
}
