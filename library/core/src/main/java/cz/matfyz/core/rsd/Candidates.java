package cz.matfyz.core.rsd;

import java.util.ArrayList;
import java.util.List;

public class Candidates {

    private static final List<String> PRIMARY_KEY_KEYWORDS = List.of("id", "code", "number", "key", "url");

    public final List<PrimaryKeyCandidate> pkCandidates = new ArrayList<>();
    public final List<ReferenceCandidate> refCandidates = new ArrayList<>();
    //public final List<RedundancyCandidate> redCandidates = new ArrayList<>();

    /**
     * Sorts primary key candidates by primary key likelihood
     */
    public void sortPkCandidates() {
        pkCandidates.sort((pk1, pk2) -> {
            String name1 = extractLastPart(pk1.hierarchicalName());
            String name2 = extractLastPart(pk2.hierarchicalName());

            return calculateScore(name2) - calculateScore(name1);
        });
    }

    private String extractLastPart(String hierarchicalName) {
        if (hierarchicalName == null || !hierarchicalName.contains("\\"))
            return hierarchicalName;

        return hierarchicalName.substring(hierarchicalName.lastIndexOf("\\") + 1);
    }

    private static int calculateScore(String columnName) {
        int score = 0;

        for (String keyword : PRIMARY_KEY_KEYWORDS)
            if (columnName.toLowerCase().contains(keyword))
                score += 10;

        if (columnName.equalsIgnoreCase("id")) {
            score += 20;
        }
        else if (columnName.toLowerCase().endsWith("id")) {
            score += 5;
        }

        return score;
    }

}
