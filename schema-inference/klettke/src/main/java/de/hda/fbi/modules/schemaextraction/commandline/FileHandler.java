package de.hda.fbi.modules.schemaextraction.commandline;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTableEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FileHandler {
    private static final DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
    private static final String SCHEMA_EXTRACTION = "schema_extraction_%s.json";
    private static final String SCHEMA_EVOLUTION = "schema_evolution_%s.txt";

    /**
     * Parses the configuration file and returns an instance of the ArgumentsParser
     *
     * @param file Configuration file for schema extraction
     * @return ArgumentsParser
     */
    public static ArgumentsParser parseConfigFile(File file) {
        ArgumentsParser argsParser = new ArgumentsParser();
        try {
            JsonParser parser = new JsonParser();
            JsonObject configFile = (JsonObject) parser.parse(new FileReader(file));

            argsParser.setHost(configFile.get("host").getAsString());
            argsParser.setPort(configFile.get("port").getAsString());
            argsParser.setDbName(configFile.get("database").getAsString());
            argsParser.setAuthDbName(configFile.get("authDb").getAsString());
            argsParser.setUsername(configFile.get("authUsername").getAsString());
            argsParser.setPassword(configFile.get("authPassword").getAsString());
            argsParser.setTsIdentifier(configFile.get("timestampIdentifier").getAsString());

            JsonArray jsonArray = configFile.get("entityTypes").getAsJsonArray();
            List<String> entityTypes = StreamSupport.stream(jsonArray.spliterator(), false)
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toList());
            argsParser.setEntityTypes(entityTypes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return argsParser;
    }

    /**
     * Writes the schemata into a json file
     */
    public static void writeSchemataToFile(List<String> schemata) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray jsonArray = new JsonParser().parse(schemata.toString()).getAsJsonArray();
        String fileName = String.format(SCHEMA_EXTRACTION, currentDateTimeAsString());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            gson.toJson(jsonArray, writer);
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Writes schema evolution operations to a text file
     */
    public static void writeEvolutionOperationsToFile(List<DecisionTableEntry> decisionEntries) {
        StringBuilder content = new StringBuilder();
        for (DecisionTableEntry entry : decisionEntries) {
            content.append(entry.getDecisionCommands().get(0).getCommand().getCommand().toString())
                    .append(System.getProperty("line.separator"));
        }

        String fileName = String.format(SCHEMA_EVOLUTION, currentDateTimeAsString());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Returns current date and time as a string
     *
     * @return String
     */
    private static String currentDateTimeAsString() {
        return dateFormat.format(new Date());
    }
}
