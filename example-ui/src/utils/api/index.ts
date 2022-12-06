import databases from "./routes/databases";
import instances from "./routes/instances";
import jobs from "./routes/jobs";
import logicalModels from "./routes/logicalModels";
import mappings from "./routes/mappings";
import models from "./routes/models";
import morphisms from "./routes/morphsims";
import objects from "./routes/objects";
import schemas from "./routes/schemas";

const API = {
    databases,
    mappings,
    jobs,
    logicalModels,
    models,
    schemas,
    instances,
    objects,
    morphisms
};

export default API;
