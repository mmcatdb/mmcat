package cz.matfyz.server.service;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.MappingRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class MappingService {

    @Autowired
    private MappingRepository repository;

    public MappingWrapper find(Id id) {
        return repository.find(id);
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        return repository.findAll(logicalModelId);
    }

    public List<MappingInfo> findAllInfos(Id logicalModelId) {
        return repository.findAllInfos(logicalModelId);
    }

    public MappingInfo createNew(MappingInit init) {
        Id generatedId = repository.add(init);

        return generatedId == null ? null : new MappingInfo(
            generatedId,
            init.kindName(),
            init.version(),
            init.categoryVersion()
        );
    }

    /**
     * Created for the case when I receive Mapping from mminfer
     * @param mapping
     * @return
     */
    public MappingInfo createNew(Mapping mapping, Id logicalModelId) {
        // probs need to obtain MappingInit first, because that's what you can save in MappingRepo
        Signature[] primaryKeyArray = mapping.primaryKey().toArray(new Signature[0]);

        MappingInit init = new MappingInit(
                logicalModelId,
                mapping.rootObject().key(),
                primaryKeyArray,
                mapping.kindName(),
                mapping.accessPath(),
                null); //Version categoryVersion (probs could use Version.generateInitial())

        Id generatedId = repository.add(init);
        //System.out.println("generatedId in MappingService: " + generatedId);
        if (generatedId != null) {
            return new MappingInfo(
                generatedId,
                mapping.kindName(),
                init.version(),
                init.categoryVersion()
            );
        }
        else {
            return null;
        }
    }
    
}
