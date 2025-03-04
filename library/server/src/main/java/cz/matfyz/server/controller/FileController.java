package cz.matfyz.server.controller;

import cz.matfyz.server.service.FileService;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.repository.FileRepository;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class FileController {

    @Autowired
    private FileService service;

    @GetMapping("/schema-categories/{categoryId}/files")
    public List<File> getAllFilesInCategory(@PathVariable Id categoryId) {
        final var files = service.findAllInCategory(categoryId);

        return files;
    }
}
