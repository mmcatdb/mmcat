package de.hda.fbi.modules.schemaextraction.configuration;

import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;

import java.util.ArrayList;
import java.util.List;

public class ExtractionConfigurationBuilder {

    private String databaseName;

    private DatabaseConfiguration databaseConfiguration;

    private List<String> entityTypes = new ArrayList<String>();

    private SchemaExtractionUseCase useCase;

    private String timestampIdentifier;

    private Object lastExtractedTimestamp;

    public ExtractionConfigurationBuilder withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public ExtractionConfigurationBuilder withDatabaseConfiguration(DatabaseConfigurationBuilder builder) {
        databaseConfiguration = builder.build();
        return this;
    }

    public ExtractionConfigurationBuilder withDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
        return this;
    }

    public ExtractionConfigurationBuilder forEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
        return this;
    }

    public ExtractionConfigurationBuilder withTimestampIdentifier(String timestampIdentifier) {
        this.timestampIdentifier = timestampIdentifier;
        return this;
    }

    public ExtractionConfigurationBuilder withLastExtractedTimestamp(Object lastExtractedTimestamp) {
        this.lastExtractedTimestamp = lastExtractedTimestamp;
        return this;
    }

    public ExtractionConfigurationBuilder withUseCase(int useCase) {

        for (SchemaExtractionUseCase useCacseValue : SchemaExtractionUseCase.values()) {
            if (useCase == useCacseValue.getValue()) {
                this.useCase = useCacseValue;
            }
        }

        return this;

    }

    public ExtractionConfiguration build() {
        return new ExtractionConfiguration(databaseConfiguration, databaseName, entityTypes, useCase,
                timestampIdentifier, lastExtractedTimestamp);
    }

    public ExtractionConfigurationBuilder forEntityTypes(String... listOfEntityTypes) {

        for (String entityType : listOfEntityTypes) {
            this.entityTypes.add(entityType);
        }

        return this;
    }
}
