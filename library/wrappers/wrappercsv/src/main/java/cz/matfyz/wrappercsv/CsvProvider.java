package cz.matfyz.wrappercsv;

public class CsvProvider {

    public final CsvSettings settings;

    public CsvProvider(CsvSettings settings) {
        this.settings = settings;
    }

    public record CsvSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
