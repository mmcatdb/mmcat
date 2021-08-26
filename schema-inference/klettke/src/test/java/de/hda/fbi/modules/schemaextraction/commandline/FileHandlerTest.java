package de.hda.fbi.modules.schemaextraction.commandline;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class FileHandlerTest {
    private FileHandler fileHandler;

    @Before
    public void setUp() throws Exception {
        fileHandler = new FileHandler();
    }

    @Test
    public void parseConfigFile() {
    }

    @Test
    public void writeSchemataToFile() {
    }

    @Test
    public void writeEvolutionOperationsToFile() {
    }

    @Test
    public void currentDateTimeAsString() {
        Date date = getDate();
        String dateTimeAsString = "29112019_153055";
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String result = dateFormat.format(date);
        assertEquals(dateTimeAsString, result);
    }

    @Test
    public void fileNamesTest() {
        String SCHEMA_EXTRACTION = "schema_extraction_%s.json";
        String SCHEMA_EVOLUTION = "schema_evolution_%s.txt";

        String expectedJsonFileName = "schema_extraction_29112019_153055.json";
        String expectedTextFileName = "schema_evolution_29112019_153055.txt";
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");

        assertEquals(expectedJsonFileName, String.format(SCHEMA_EXTRACTION, dateFormat.format(getDate())));
        assertEquals(expectedTextFileName, String.format(SCHEMA_EVOLUTION, dateFormat.format(getDate())));
    }

    private Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2019);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 29);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 55);
        return cal.getTime();
    }
}