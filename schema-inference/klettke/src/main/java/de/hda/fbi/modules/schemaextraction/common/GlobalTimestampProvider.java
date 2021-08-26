package de.hda.fbi.modules.schemaextraction.common;

public class GlobalTimestampProvider {

    private static final GlobalTimestampProvider globalTimestampProvider = new GlobalTimestampProvider();

    int timestamp = 0;

    public static GlobalTimestampProvider getInstance() {
        return globalTimestampProvider;
    }

    public int getNextTimestamp() {
        return ++timestamp;
    }

    public void clear() {
        this.timestamp = 0;
    }
}
