package de.hda.fbi.modules.schemaextraction.commandline;

import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Arguments (Parameters) parser for the Schema-Extraction CLI
 * Each
 */
@Component
@Command(showDefaultValues = true)
public class ArgumentsParser {
    @Option(names = {"--help"}, usageHelp = true, description = "Help")
    private boolean help = false;

    @Option(names = {"-h", "--host"}, description = "Host", paramLabel = "HOST")
    private String host = "localhost";

    @Option(names = {"-p", "--port"}, description = "Port", paramLabel = "PORT")
    private String port = "27017";

    @Option(names = {"-d", "--db-name"}, description = "Database name", paramLabel = "DB_NAME")
    private String dbName = "species";

    @Option(names = {"-a", "--db-auth"}, description = "Auth database name", paramLabel = "AUTH_DB_NAME")
    private String authDbName = "admin";

    @Option(names = {"-u", "--username"}, description = "Database user name", paramLabel = "DB_USERNAME")
    private String username = "darwin";

    @Option(names = {"-pw", "--password"}, description = "Database password", paramLabel = "DB_PASSWORD",
            showDefaultValue = CommandLine.Help.Visibility.NEVER)
    private String password = "darwin_test";

    @Option(names = {"-e", "--entities"}, split = ",", description = "Entity types (comma separated values)",
            paramLabel = "ENTITY_TYPES")
    private List<String> entityTypes = new ArrayList<>(Arrays.asList("Protocols", "Species"));

    @Option(names = {"-ts", "--timestamp"}, description = "Timestamp identifier", paramLabel = "TS_IDENTIFIER")
    private String tsIdentifier = "ts";

    @Option(names = {"-f", "--file"}, description = "Configuration file for the schema extraction",
            paramLabel = "FILE_PATH")
    private File file;

    public ArgumentsParser() {
    }

    public boolean isHelp() {
        return help;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDbName() {
        return dbName;
    }

    public String getAuthDbName() {
        return authDbName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getEntityTypes() {
        return entityTypes;
    }

    public String getTsIdentifier() {
        return tsIdentifier;
    }

    public File getFile() {
        return file;
    }

    public boolean isFileAvailable() {
        return file != null;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setAuthDbName(String authDbName) {
        this.authDbName = authDbName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public void setTsIdentifier(String tsIdentifier) {
        this.tsIdentifier = tsIdentifier;
    }
}
