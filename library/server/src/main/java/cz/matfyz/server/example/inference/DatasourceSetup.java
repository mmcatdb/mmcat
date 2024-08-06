package cz.matfyz.server.example.inference;

import cz.matfyz.server.Configuration.SetupProperties;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.example.common.DatasourceSettings;
import cz.matfyz.server.service.DatasourceService;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.shaded.org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("inferenceDatasourceSetup")
class DatasourceSetup {

    private final DatasourceSettings settings;
    private final DatasourceService datasourceService;

    @Autowired
    DatasourceSetup(SetupProperties properties, DatasourceService datasourceService) {
        this.settings = new DatasourceSettings(properties, properties.inferenceDatabase());
        this.datasourceService = datasourceService;
    }

    DatasourceWrapper createDatasource() {
        DatasourceInit init = settings.createMongoDB("MongoDB - Inference");
        return datasourceService.createNew(init);
    }

    List<DatasourceWrapper> createDatasourceForMapping() {
        throw new NotImplementedException("DatasourceSetup.createDatasourceForMapping() in inference is not implemented");
    }

}
