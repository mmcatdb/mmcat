package de.hda.fbi.modules.schemaextraction.decision;

import java.io.Serializable;
import java.util.*;

/**
 * @author daniel.mueller
 * <p>
 * Entry Entry Entry (Conflict) Command Command Entry Entry (Conflict) Command Command
 */
public class DecisionTable implements Serializable {

    private static final long serialVersionUID = -928340739827786991L;

    public Vector<DecisionTableEntry> decisionEntries = new Vector<DecisionTableEntry>();

    private Vector<Integer> availableTimestamps;

    private Map<Integer, Set<Object>> timestampWithDocumentIds;

    private Map<Integer, Vector<Object>> timestampWithTimestamp;

    public int getDecisionCommandCount() {

        int count = 0;
        for (DecisionTableEntry entry : decisionEntries) {

            count += entry.getDecisionCommands().size();
        }

        return count;
    }

    public void printDecisionTable() {
        for (DecisionTableEntry entry : decisionEntries) {
            for (DecisionCommand dCmd : entry.getDecisionCommands()) {
                System.out.println(dCmd.getCommand().getCommand());
            }
        }
    }

    public Integer getMaxTimestamp() {
        Integer ts = 0;

        Vector<Integer> timestamps = this.getSortedAvailableTimestamps();

        for (Integer timestamp : timestamps) {
            if (timestamp > ts) {
                ts = timestamp;
            }
        }
        return ts;
    }

    public Vector<Integer> getSortedAvailableTimestamps() {

        if (this.availableTimestamps == null) {
            this.availableTimestamps = new Vector<Integer>();
            for (DecisionTableEntry entry : decisionEntries) {
                if (!availableTimestamps.contains(entry.getTimestamp())) {
                    availableTimestamps.add(entry.getTimestamp());
                }
            }

            Collections.sort(availableTimestamps);
        }

        return this.availableTimestamps;

    }

    public List<Integer> getAllTimestampsSorted() {
        List<Integer> timestamps = new ArrayList<>();
        timestampWithDocumentIds.entrySet().forEach(entry -> {
            // no check for duplicate needed because maps cant have duplicate keys.
            timestamps.add(entry.getKey());
            entry.getValue().forEach(subEntry -> {
                Integer value = (Integer) subEntry;
                if (!timestamps.contains(value)) {
                    timestamps.add(value);
                }
            });
        });
        Collections.sort(timestamps);
        return timestamps;
    }

    public boolean isClarified() {
        for (DecisionTableEntry entry : decisionEntries) {
            if (entry.isClarified() == false) {
                return false;
            }
        }

        return true;
    }

    public DecisionTableEntry getDecisionTableEntry(int decisionTableEntryId) {
        for (DecisionTableEntry entry : decisionEntries) {
            if (entry.getId() == decisionTableEntryId) {
                return entry;
            }
        }

        return null;
    }

    public Vector<DecisionTableEntry> getDecisionTableEntriesForTimestamp(Integer timestamp) {
        this.sortAscending();
        Vector<DecisionTableEntry> entries = new Vector<>();
        for (DecisionTableEntry decisionTableEntry : decisionEntries) {

            if (decisionTableEntry.getTimestamp().equals(timestamp)) {
                entries.addElement(decisionTableEntry);
            }
        }

        return entries;
    }

    public Vector<DecisionTableEntry> getDecisionTableEntriesForTimestampsBetween(Integer startTimestamp,
                                                                                  Integer endTimestamp) {

        Vector<DecisionTableEntry> entries = new Vector<DecisionTableEntry>();
        for (DecisionTableEntry decisionTableEntry : decisionEntries) {

            if (decisionTableEntry.getTimestamp() > startTimestamp
                    && decisionTableEntry.getTimestamp() < endTimestamp) {
                entries.addElement(decisionTableEntry);
            }
        }

        return entries;
    }

    /**
     * Sorts the decision entries ascending based on the timestamp
     */
    public void sortAscending() {

        Collections.sort(decisionEntries, (p1, p2) -> {
            int timestamp = p1.getTimestamp().compareTo(p2.getTimestamp());
            if (timestamp != 0) {
                return timestamp;
            }
            DecisionCommand cmdWithInfo1;
            DecisionCommand cmdWithInfo2;

            if (p1.getDecisionCommands().size() > 0 && p2.getDecisionCommands().size() > 0) {
                cmdWithInfo1 = p1.getDecisionCommands().get(0);
                cmdWithInfo2 = p2.getDecisionCommands().get(0);
                return cmdWithInfo1.getCommand().getGlobalTimestamp()
                        .compareTo(cmdWithInfo2.getCommand().getGlobalTimestamp());
            } else {
                return timestamp;
            }
        });
    }

    public void sortDesending() {

        Collections.sort(decisionEntries, (p1, p2) -> {
            int timestamp = p2.getTimestamp().compareTo(p1.getTimestamp());
            if (timestamp != 0) {
                return timestamp;
            }
            DecisionCommand cmdWithInfo1 = p1.getDecisionCommands().get(0);
            DecisionCommand cmdWithInfo2 = p2.getDecisionCommands().get(0);
            return cmdWithInfo2.getCommand().getGlobalTimestamp()
                    .compareTo(cmdWithInfo1.getCommand().getGlobalTimestamp());

        });
    }

    public Map<Integer, Set<Object>> getTimestampWithDocumentIds() {
        return timestampWithDocumentIds;
    }

    public Map<Integer, Vector<Object>> getTimestampWithTimestamp() {
        return timestampWithTimestamp;
    }

    public Vector<DecisionTableEntry> getDecisionEntries() {
        return decisionEntries;
    }

    public void setDecisionEntries(Vector<DecisionTableEntry> decisionEntries) {
        this.decisionEntries = decisionEntries;
    }

    public void setTimestampWithDocumentIds(Map<Integer, Set<Object>> timestampWithDocumentIds) {
        this.timestampWithDocumentIds = timestampWithDocumentIds;
    }

    public void setTimestampWithTimestamp(Map<Integer, Vector<Object>> timestampWithTimestamp) {
        this.timestampWithTimestamp = timestampWithTimestamp;
    }
}
