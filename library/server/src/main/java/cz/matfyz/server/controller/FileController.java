package cz.matfyz.server.controller;

import cz.matfyz.server.service.FileService;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.file.File;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import cz.matfyz.server.global.Configuration.UploadsProperties;
import cz.matfyz.server.repository.FileRepository;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

@RestController
public class FileController {

    @Autowired
    private FileService service;

    @Autowired
    private FileRepository repository;

    @Autowired
    private UploadsProperties uploads;

    @GetMapping("/schema-categories/{categoryId}/files")
    public List<File> getAllFilesInCategory(@PathVariable Id categoryId) {
        final var files = service.findAllInCategory(categoryId);

        return files;
    }

    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Id id) {
        final File file = repository.find(id);

        try {
            final Path filePath = Paths.get(File.getFilePath(file, uploads));
            final Resource resource = new UrlResource(filePath.toUri());

            return  ResponseEntity.ok() // TODO: Unfortunatelly, these settings are never used.
                    .header("fileType", file.fileType.toString())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/files/{id}/execute")
    public void executeDML(@PathVariable Id id) {
        final File file = repository.find(id);
        service.executeDML(file);
    }

    @PutMapping("files/{id}/update")
    public File updateFile(@PathVariable Id id, @RequestBody Map<String, Object> body) {
        final String value = ((String) body.get("value")).trim();
        final boolean isLabel = (boolean) body.get("isLabel");

        return service.updateFile(id, value, isLabel);
    }

}
