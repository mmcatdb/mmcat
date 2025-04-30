package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a table response.
 */
public class TableResponse extends DataResponse {
    // FIXME Je to skutečně mapa stringů? Co další typy? Resp. všechno musíte nakonec nějak převést na string, nicméně možná by potom chtělo pamatovat si ty typy např. v metadatech.
    // Nebylo by efektivnějí předávat to jako List<List<String>>? Ty názvy sloupců máte v metadatech, ne? Ale klidně je můžete předat ještě tady jako List<String>.
    private List<Map<String, String>> data;

    public TableResponse(List<Map<String, String>> data, int itemCount, Set<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<Map<String, String>> getData() {
        return data;
    }
}
