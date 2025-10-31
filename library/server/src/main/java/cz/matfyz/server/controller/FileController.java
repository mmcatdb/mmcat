package cz.matfyz.server.controller;

import cz.matfyz.server.service.FileService;
import cz.matfyz.server.service.FileService.FileEdit;
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

            return ResponseEntity.ok() // TODO: Unfortunatelly, these settings are never used.
                .header("fileType", file.fileType.toString())
                .body(resource);
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/files/{id}/execute")
    public File executeDML(@PathVariable Id id, @RequestBody Map<String, String> body) {
        final File file = repository.find(id);

        final String mode = body.get("mode");
        final String newDBName = body.get("newDBName");

        return service.executeDML(file, mode, newDBName);
    }

    @PutMapping("files/{id}/update")
    public File updateFile(@PathVariable Id id, @RequestBody FileEdit edit) {
        return service.updateFile(id, edit);
    }

    @GetMapping("files/{id}/preview")
    public String previewFile(@PathVariable Id id) {
        return service.readPreview(id);
    }

}
