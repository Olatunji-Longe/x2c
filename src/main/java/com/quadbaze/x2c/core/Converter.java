package com.quadbaze.x2c.core;

import com.quadbaze.x2c.core.streams.ConservableBufferOutputStream;
import com.quadbaze.x2c.core.streams.UploadableInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Olatunji O. Longe on 04/05/2018 4:25 PM
 */
public class Converter {

    /**
     * Nicely converts (.xls) and (.xlsx) document inputStream to a csv inputStream
     * @param xlsFileInputStream
     * @return
     */
    public static UploadableInputStream convertToCsv(InputStream xlsFileInputStream) {
        UploadableInputStream uploadableInputStream = null;
        PrintStream printStream = null;
        String message = "";
        try{
            Workbook workbook = WorkbookFactory.create(xlsFileInputStream);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter();
            ConservableBufferOutputStream outputStream = new ConservableBufferOutputStream();
            printStream = new PrintStream(outputStream, true, "UTF-8");

            // Required so that the csv file can be loaded/read in excel
            printStream.write(getByteOrderMarker());

            if(workbook.getNumberOfSheets() > 0){
                for(int sheetNo = 0; sheetNo < workbook.getNumberOfSheets(); sheetNo++) {
                    Sheet sheet = workbook.getSheetAt(sheetNo);

                    System.out.println("Sheet-Name =====: " + sheet.getSheetName());

                    int rowNum = sheet.getLastRowNum();
                    for (int r = 0; r <= rowNum; r++) {
                        Row row = sheet.getRow(r);
                        if(row != null){
                            boolean firstCell = true;
                            short colNum = row.getLastCellNum();
                            for (int c = 0; c < colNum; c++) {
                                Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                                if (!firstCell) {
                                    printStream.print(',');
                                }
                                if (cell != null) {
                                    if (formulaEvaluator != null) {
                                        cell = formulaEvaluator.evaluateInCell(cell);
                                    }
                                    String value = formatter.formatCellValue(cell);
                                    if (cell.getCellTypeEnum() == CellType.FORMULA) {
                                        value = "=" + value;
                                    }
                                    printStream.print(formatValue(value));
                                }
                                firstCell = false;
                            }
                            printStream.println();
                        }else{
                            printStream.println(',');
                        }
                    }

                    uploadableInputStream = outputStream.toInputStream();
                    message = String.format("Conversion completed for %s =====: ", sheet.getSheetName());
                }
            }else{
                message = String.format("No sheets!");
            }
        } catch(IOException | InvalidFormatException ex){
            message = String.format("Conversion failed due to: %s =====: ", ex.getMessage());
            ex.printStackTrace();
        }finally {
            if(printStream != null){
                printStream.close();
                message = message + " | Closed the output stream!";
            }

            System.out.println(message);
        }
        return uploadableInputStream;
    }

    /**
     * Excel needs a Byte-Order-Marker to indicate that the file is encoded in UTF-8
     * (NOTE - Without this, the csv file may not be loadable again in Excel)
     * @return
     */
    private static byte[] getByteOrderMarker(){
        return new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
    }

    /**
     * Handles escaping of double quotes, commas, and line separators.
     * When exporting a field value, it is necessary to properly escape these characters
     * @param value field/cell value to escape
     * @return
     */
    private static String formatValue(String value) {
        boolean shouldQuote = (value.indexOf(',') != -1 || value.indexOf('"') != -1 || value.indexOf('\n') != -1 || value.indexOf('\r') != -1);

        Matcher matcher = Pattern.compile("\"").matcher(value);
        if (matcher.find()) {
            shouldQuote = true;
            value = matcher.replaceAll("\"\"");
        }

        if (shouldQuote) {
            return "\"" + value + "\"";
        }else {
            return value;
        }
    }


}
