package cz.matfyz.evolution.metadata;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObject;

import java.util.Map;

// TODO unify all schema editors in some way ...
public class MetadataEditor extends MetadataCategory.Editor {

    private final MetadataCategory metadata;

    public MetadataEditor(MetadataCategory metadata) {
        this.metadata = metadata;
    }

    public Map<Key, MetadataObject> getObjects() {
        return getObjects(metadata);
    }

    public Map<Signature, MetadataMorphism> getMorphisms() {
        return getMorphisms(metadata);
    }

}
