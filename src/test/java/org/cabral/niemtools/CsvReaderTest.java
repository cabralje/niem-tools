package org.cabral.niemtools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CsvReaderTest {

    private File tempFile;

    @Before
    public void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testImportCsvFileNotFound() {
        CsvReader reader = new CsvReader();
        reader.importCsv("nonexistent_file.csv");
        // Should not throw, should log error
    }

    @Test
    public void testImportCsvMalformedCsv() throws Exception {
        // Write a malformed CSV
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("not,a,valid,csv\n\"unterminated");
        }
        CsvReader reader = new CsvReader();
        reader.importCsv(tempFile.getAbsolutePath());
        // Should not throw, should log error
    }

    @Test
    public void testImportCsvEmptyFile() throws Exception {
        // Write an empty file
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("");
        }
        CsvReader reader = new CsvReader();
        reader.importCsv(tempFile.getAbsolutePath());
        // Should not throw
    }

    @Test
    public void testImportCsvHeaderOnly() throws Exception {
        // Write only a header row (should be skipped, no data rows)
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("ClassName,AttributeName,Type,Multiplicity,Definition,XPath,NIEMType,Property,BaseType,NIEMMultiplicity,OldXPath,OldMultiplicity,Notes,CodeList\n");
        }
        CsvReader reader = new CsvReader();
        reader.importCsv(tempFile.getAbsolutePath());
        // Should not throw, should process zero data rows
    }

    @Test
    public void testImportCsvWithExtraColumns() throws Exception {
        // CSV with more columns than expected
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("Col1,Col2,Col3,Col4,Col5,Col6,Col7,Col8,Col9,Col10,Col11,Col12,Col13,Col14,ExtraCol\n");
            fw.write("Class1,,,,desc,xpath,type,prop,base,1,,,,code,extra\n");
        }
        CsvReader reader = new CsvReader();
        reader.importCsv(tempFile.getAbsolutePath());
        // Should not throw
    }

    @Test
    public void testImportCsvWithSpecialCharacters() throws Exception {
        // CSV with special characters in values
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("Col1,Col2,Col3,Col4,Col5,Col6,Col7,Col8,Col9,Col10,Col11,Col12,Col13,Col14\n");
            fw.write("\"Class with, comma\",\"attr \"\"quoted\"\"\",type,1,\"desc with\nnewline\",xpath,type,prop,base,1,,,notes,code\n");
        }
        CsvReader reader = new CsvReader();
        reader.importCsv(tempFile.getAbsolutePath());
        // Should not throw
    }

    @Test
    public void testImportCsvWithFewerColumns() throws Exception {
        // CSV with fewer columns than expected
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("Col1,Col2\n");
            fw.write("Class1,attr1\n");
        }
        CsvReader reader = new CsvReader();
        reader.importCsv(tempFile.getAbsolutePath());
        // Should not throw
    }

    @Test
    public void testConstructor() {
        CsvReader reader = new CsvReader();
        org.junit.Assert.assertNotNull(reader);
    }
}
