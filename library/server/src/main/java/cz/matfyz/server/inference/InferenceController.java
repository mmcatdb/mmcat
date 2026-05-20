package cz.matfyz.server.inference;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
import cz.matfyz.server.job.JobController;
import cz.matfyz.server.job.JobRepository;
import cz.matfyz.server.job.JobController.JobDetail;
import cz.matfyz.server.job.JobRepository.JobWithRun;
import cz.matfyz.server.utils.entity.Id;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class InferenceController {

    @Autowired
    private InferenceService service;

    @Autowired
    private JobRepository repository;

    @Autowired
    private JobController jobController;

    private record SaveJobResultPayload(
        @Nullable boolean isFinal,
        @Nullable InferenceEdit edit,
        @Nullable LayoutType layoutType,
        @Nullable List<PositionUpdate> positions
    ) {}

    private record PositionUpdate(
        Key key,
        Position position
    ) {}

    @PostMapping("/inference-jobs/{id}/update-result")
    public JobDetail updateJobResult(@PathVariable Id id, @RequestBody SaveJobResultPayload payload) {
        final var jobWithRun = repository.find(id);
        if (!(jobWithRun.job().data instanceof InferenceJobData inferenceJobData))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The job data is not an instance of InferenceJobData");

        final @Nullable Map<Key, Position> positionsMap = payload.positions() == null ? null : extractPositions(payload.positions());

        final @Nullable InferenceEdit edit = payload.isFinal() ? null : payload.edit();

        final JobWithRun newJobWithRun = service.updateJob(
            jobWithRun,
            inferenceJobData,
            edit,
            payload.isFinal(),
            payload.layoutType(),
            positionsMap
        );

        repository.save(jobWithRun.job());

        return jobController.jobToJobDetail(newJobWithRun);
    }

    private Map<Key, Position> extractPositions(List<PositionUpdate> positionUpdates) {
        final Map<Key, Position> positionsMap = new HashMap<>();
        for (final PositionUpdate positionUpdate : positionUpdates)
            positionsMap.put(positionUpdate.key(), positionUpdate.position());

        return positionsMap;
    }

}
