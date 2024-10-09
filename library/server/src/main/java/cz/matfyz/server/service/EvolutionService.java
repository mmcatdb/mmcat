package cz.matfyz.server.service;

import cz.matfyz.server.repository.EvolutionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvolutionService {

    @Autowired
    private EvolutionRepository repository;

}
