/**
 *
 * @author Wei-Ming Wu
 *
 *
 * Copyright 2013 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.github.wnameless.workbookaccessor;

import static com.google.common.base.Preconditions.checkArgument;
import static net.sf.rubycollect4j.RubyCollections.newRubyArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.github.wnameless.nullproof.annotation.RejectNull;
import com.google.common.base.Objects;

/**
 * 
 * {@link WorkbookWriter} is a wrapper to Apache POI. It tends to provide
 * friendly APIs for workbook writing.
 * 
 */
@RejectNull
public final class WorkbookWriter {

  private static final Logger logger = Logger.getLogger(WorkbookWriter.class
      .getName());

  private final Workbook workbook;
  private Sheet sheet;

  /**
   * Returns a {@link WorkbookWriter} with XLS format.
   * 
   * @return a {@link WorkbookWriter}
   */
  public static WorkbookWriter openXLS() {
    return new WorkbookWriter();
  }

  /**
   * Returns a {@link WorkbookWriter} with XLSX format.
   * 
   * @return a {@link WorkbookWriter}
   */
  public static WorkbookWriter openXLSX() {
    return new WorkbookWriter(true);
  }

  /**
   * Returns a {@link WorkbookWriter} by given {@link Workbook}.
   * 
   * @param workbook
   *          a {@link Workbook}
   * @return a {@link WorkbookWriter}
   */
  public static WorkbookWriter open(Workbook workbook) {
    return new WorkbookWriter(workbook);
  }

  /**
   * Creates a {@link WorkbookWriter}. Default sheet name is Sheet0 and XLS
   * format is used.
   */
  public WorkbookWriter() {
    workbook = new HSSFWorkbook();
    sheet = workbook.createSheet();
  }

  /**
   * Creates a {@link WorkbookWriter} by given {@link Workbook}.
   * 
   * @param workbook
   *          a {@link Workbook}
   */
  public WorkbookWriter(Workbook workbook) {
    this.workbook = workbook;
    if (workbook.getNumberOfSheets() == 0)
      workbook.createSheet();
    sheet = workbook.getSheetAt(0);
  }

  /**
   * Creates a {@link WorkbookWriter}.
   * 
   * @param xlsx
   *          true if a xlsx file is used, false otherwise
   * @deprecated use {@link WorkbookWriter#openXLSX WorkbookWriter.openXLSX()}
   *             instead
   */
  @Deprecated
  public WorkbookWriter(boolean xlsx) {
    workbook = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
    sheet = workbook.createSheet();
  }

  /**
   * Creates a {@link WorkbookWriter} and creates a new sheet by given name.
   * 
   * @param sheetName
   *          name of a sheet
   * @deprecated use {@link #setSheetName(String)} instead
   */
  @Deprecated
  public WorkbookWriter(String sheetName) {
    workbook = new HSSFWorkbook();
    sheet = workbook.createSheet(sheetName);
  }

  /**
   * Sets current sheet name to given name.
   * 
   * @param sheetName
   *          name of a sheet
   * @return this {@link WorkbookWriter}
   */
  public WorkbookWriter setSheetName(String sheetName) {
    workbook.setSheetName(workbook.getSheetIndex(sheet.getSheetName()),
        sheetName);
    return this;
  }

  /**
   * Returns the backing POI {@link Workbook}.
   * 
   * @return the POI {@link Workbook}
   */
  public Workbook getWorkbook() {
    return workbook;
  }

  /**
   * Returns the name of current sheet.
   * 
   * @return name of current sheet
   */
  public String getCurrentSheetName() {
    return sheet.getSheetName();
  }

  /**
   * Returns a List which contains all sheet names.
   * 
   * @return a String List
   */
  public List<String> getAllSheetNames() {
    List<String> sheets = newRubyArray();
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      sheets.add(workbook.getSheetName(i));
    }
    return sheets;
  }

  /**
   * Creates a new sheet.
   * 
   * @param sheetName
   *          name of a sheet
   * @return this {@link WorkbookWriter}
   */
  public WorkbookWriter createSheet(String sheetName) {
    checkArgument(!getAllSheetNames().contains(sheetName),
        "Sheet name is already existed.");
    workbook.createSheet(sheetName);
    return this;
  }

  /**
   * Turns this {@link WorkbookWriter} to certain sheet. Sheet names can be
   * found by {@link #getAllSheetNames}.
   * 
   * @param index
   *          of a sheet
   * @return this {@link WorkbookWriter}
   */
  public WorkbookWriter turnToSheet(int index) {
    sheet = workbook.getSheetAt(index);
    return this;
  }

  /**
   * Creates a new sheet and turns this {@link WorkbookWriter} to the sheet.
   * 
   * @param sheetName
   *          of a sheet
   * @return this {@link WorkbookWriter}
   */
  public WorkbookWriter turnToSheet(String sheetName) {
    checkArgument(getAllSheetNames().contains(sheetName),
        "Sheet name is not found.");
    return turnToSheet(getAllSheetNames().indexOf(sheetName));
  }

  /**
   * Turns this {@link WorkbookWriter} to certain sheet. Sheet names can be
   * found by {@link #getAllSheetNames}.
   * 
   * @param sheetName
   *          name of a sheet
   * @return this {@link WorkbookWriter}
   */
  public WorkbookWriter createAndTurnToSheet(String sheetName) {
    checkArgument(!getAllSheetNames().contains(sheetName),
        "Sheet name is already existed.");
    sheet = workbook.createSheet(sheetName);
    return this;
  }

  /**
   * Adds a row to the sheet.
   * 
   * @param fields
   *          an Iterable of Object
   * @return this {@link WorkbookWriter}
   */
  public WorkbookWriter addRow(Iterable<? extends Object> fields) {
    Row row;
    if (sheet.getLastRowNum() == 0 && sheet.getPhysicalNumberOfRows() == 0)
      row = sheet.createRow(0);
    else
      row = sheet.createRow(sheet.getLastRowNum() + 1);

    int i = 0;
    for (Object o : fields) {
      Cell cell = row.createCell(i);
      if (o != null) {
        if (o instanceof Boolean)
          cell.setCellValue((Boolean) o);
        else if (o instanceof Calendar)
          cell.setCellValue((Calendar) o);
        else if (o instanceof Date)
          cell.setCellValue((Date) o);
        else if (o instanceof Double)
          cell.setCellValue((Double) o);
        else if (o instanceof RichTextString)
          if ((o instanceof HSSFRichTextString && workbook instanceof HSSFWorkbook)
              || (o instanceof XSSFRichTextString && workbook instanceof XSSFWorkbook)) {
            cell.setCellValue((RichTextString) o);
          } else {
            cell.setCellValue(o.toString());
          }
        else if (o instanceof Hyperlink)
          cell.setHyperlink((Hyperlink) o);
        else if (o instanceof Number)
          cell.setCellValue(((Number) o).doubleValue());
        else
          cell.setCellValue(o.toString());
      }
      i++;
    }
    return this;
  }

  /**
   * Adds a row to the sheet.
   * 
   * @param fields
   *          an array of Object
   * @return this {@link WorkbookWriter}
   */
  public WorkbookWriter addRow(Object... fields) {
    return addRow(Arrays.asList(fields));
  }

  /**
   * Saves this {@link WorkbookWriter} to a file.
   * 
   * @param path
   *          of the output file
   * @return a saved File
   */
  public File save(String path) {
    try {
      FileOutputStream out = new FileOutputStream(path);
      workbook.write(out);
      out.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, null, e);
      throw new RuntimeException(e);
    }
    return new File(path);
  }

  /**
   * Converts this {@link WorkbookWriter} to a {@link WorkbookReader}.
   * 
   * @return a {@link WorkbookReader}
   */
  public WorkbookReader toReader() {
    return new WorkbookReader(workbook);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof WorkbookWriter) {
      WorkbookWriter writer = (WorkbookWriter) o;
      return Objects.equal(toReader(), writer.toReader());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(toReader());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).addValue(toReader().toMultimap())
        .toString();
  }

}
