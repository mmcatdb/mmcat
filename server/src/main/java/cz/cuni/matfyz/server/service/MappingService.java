package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.repository.MappingRepository;
import cz.cuni.matfyz.server.entity.Position;
import cz.cuni.matfyz.server.entity.MappingWrapper;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author jachym.bartik
 */
@Service
public class MappingService
{
    @Autowired
    private MappingRepository repository;

    public List<MappingWrapper> findAllInCategory(int categoryId)
    {
        return repository.findAllInCategory(categoryId);
    }

    public MappingWrapper find(int id)
    {
        return repository.find(id);
    }
}
