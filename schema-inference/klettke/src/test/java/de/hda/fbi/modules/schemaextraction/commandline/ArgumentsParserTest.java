package de.hda.fbi.modules.schemaextraction.commandline;

import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArgumentsParserTest {

    private ArgumentsParser argsParser;

    @Before
    public void setUp() throws Exception {
        argsParser = new ArgumentsParser();
    }

    @Test
    public void argumentsParserTest() {
        String host = "127.0.0.2";
        String port = "12345";
        String dbName = "University";
        String username = "User2019";
        String entityTypes = "Students,Bikes";

        CommandLine cmd = new CommandLine(argsParser);
        cmd.parseArgs(String.format("--host=%s", host),
                String.format("--port=%s", port),
                String.format("--db-name=%s", dbName),
                String.format("--username=%s", username),
                String.format("--entities=%s", entityTypes));

        assertEquals(host, argsParser.getHost());
        assertEquals(port, argsParser.getPort());
        assertEquals(dbName, argsParser.getDbName());
        assertEquals(username, argsParser.getUsername());
        assertEquals(new ArrayList<>(Arrays.asList(entityTypes.split(","))), argsParser.getEntityTypes());

    }

    @Test
    public void setHost() {
        String newHost = "127.0.0.2";
        argsParser.setHost(newHost);
        assertEquals(newHost, argsParser.getHost());
    }

    @Test
    public void setPort() {
        String newPort = "27000";
        argsParser.setPort(newPort);
        assertEquals(newPort, argsParser.getPort());
    }

    @Test
    public void setDbName() {
        String newDbName = "darwin2";
        argsParser.setDbName(newDbName);
        assertEquals(newDbName, argsParser.getDbName());
    }

    @Test
    public void setAuthDbName() {
        String newAuthDbName = "not-an-admin";
        argsParser.setAuthDbName(newAuthDbName);
        assertEquals(newAuthDbName, argsParser.getAuthDbName());
    }

    @Test
    public void setUsername() {
        String newUsername = "user005";
        argsParser.setUsername(newUsername);
        assertEquals(newUsername, argsParser.getUsername());
    }

    @Test
    public void setEntityTypes() {
        List<String> newEntityList = new ArrayList<>(Arrays.asList("Autos", "Bikes"));
        argsParser.setEntityTypes(newEntityList);
        assertEquals(newEntityList, argsParser.getEntityTypes());
    }

    @Test
    public void setTsIdentifier() {
        String newTsIdentifier = "t_s_";
        argsParser.setTsIdentifier(newTsIdentifier);
        assertEquals(newTsIdentifier, argsParser.getTsIdentifier());
    }

    @Test
    public void isHelp() {
        assertEquals(false, argsParser.isHelp());
    }

    @Test
    public void getHost() {
        assertEquals("localhost", argsParser.getHost());
    }

    @Test
    public void getPort() {
        assertEquals("27017", argsParser.getPort());
    }

    @Test
    public void getDbName() {
        assertEquals("species", argsParser.getDbName());
    }

    @Test
    public void getAuthDbName() {
        assertEquals("admin", argsParser.getAuthDbName());
    }

    @Test
    public void getUsername() {
        assertEquals("darwin", argsParser.getUsername());
    }

    @Test
    public void getEntityTypes() {
        assertEquals(new ArrayList<>(Arrays.asList("Protocols", "Species")), argsParser.getEntityTypes());
    }

    @Test
    public void getTsIdentifier() {
        assertEquals("ts", argsParser.getTsIdentifier());
    }
}