package de.hda.fbi.modules.schemaextraction.configuration;

import de.hda.fbi.modules.schemaextraction.common.LastExtractedSchema;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;

import java.util.ArrayList;
import java.util.List;

public class SchemaExtractionConfigurationBuilder {

    private String databaseName;

    private DatabaseConfiguration databaseConfiguration;

    private List<String> entityTypes = new ArrayList<String>();

    private List<LastExtractedSchema> lastExtractedSchemas = new ArrayList<LastExtractedSchema>();

    private SchemaExtractionUseCase useCase;

    private String timestampIdentifier;

    private Object lastExtractedTimestamp;

    public SchemaExtractionConfigurationBuilder withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public SchemaExtractionConfigurationBuilder withDatabaseConfiguration(DatabaseConfigurationBuilder builder) {
        databaseConfiguration = builder.build();
        return this;
    }

    public SchemaExtractionConfigurationBuilder withDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
        return this;
    }

    public SchemaExtractionConfigurationBuilder forEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
        return this;
    }

    public SchemaExtractionConfigurationBuilder addLastExtractedSchema(LastExtractedSchema lastExtractedSchema) {

        if (this.lastExtractedSchemas != null) {
            this.lastExtractedSchemas.add(lastExtractedSchema);
        }

        return this;
    }

    public SchemaExtractionConfigurationBuilder withTimestampIdentifier(String timestampIdentifier) {
        this.timestampIdentifier = timestampIdentifier;
        return this;
    }

    public SchemaExtractionConfigurationBuilder withLastExtractedTimestamp(Object lastExtractedTimestamp) {
        this.lastExtractedTimestamp = lastExtractedTimestamp;
        return this;
    }

    public SchemaExtractionConfigurationBuilder withUseCase(int useCase) {

        for (SchemaExtractionUseCase useCacseValue : SchemaExtractionUseCase.values()) {
            if (useCase == useCacseValue.getValue()) {
                this.useCase = useCacseValue;
            }
        }

        return this;

    }

    public SchemaExtractionConfiguration build() {
        return new SchemaExtractionConfiguration(databaseConfiguration, databaseName, entityTypes, useCase,
                timestampIdentifier, lastExtractedTimestamp, this.lastExtractedSchemas);
    }

    public SchemaExtractionConfigurationBuilder forEntityTypes(String... listOfEntityTypes) {

        for (String entityType : listOfEntityTypes) {
            this.entityTypes.add(entityType);
        }

        return this;
    }
}
