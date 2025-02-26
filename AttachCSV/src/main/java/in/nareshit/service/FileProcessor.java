package in.nareshit.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.InputStreamReader;
import java.io.Reader;



import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class FileProcessor {

    public List<List<String>> processFile(MultipartFile file, int startRow) throws IOException {
        String contentType = file.getContentType();
        startRow = Math.max(startRow - 1, 0);

        if (contentType == null) {
            throw new IllegalArgumentException("File type not recognized");
        }

        if (contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")) {
            return processCSV(file, startRow);
        } else if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return processExcel(file, startRow);
        }

        throw new IllegalArgumentException("Unsupported file format");
    }

    private List<List<String>> processCSV(MultipartFile file, int startRow) throws IOException {
        List<List<String>> data = new ArrayList<>();
        
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            // Add headers
            data.add(new ArrayList<>(csvParser.getHeaderNames()));
            
            // Add rows
            for (CSVRecord record : csvParser) {
                if (record.getRecordNumber() >= startRow) {
                    List<String> row = new ArrayList<>();
                    record.forEach(row::add);
                    data.add(row);
                }
            }
        }
        return data;
    }

    private List<List<String>> processExcel(MultipartFile file, int startRow) throws IOException {
        List<List<String>> data = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        // Process headers
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.toString());
        }
        data.add(headers);

        // Process data rows
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() < startRow) continue;
            
            List<String> rowData = new ArrayList<>();
            for (Cell cell : row) {
                rowData.add(cell.toString());
            }
            data.add(rowData);
        }
        workbook.close();
        return data;
    }
}
