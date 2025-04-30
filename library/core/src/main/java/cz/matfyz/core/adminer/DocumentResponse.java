package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a document response.
 */
public class DocumentResponse extends DataResponse {
    // FIXME Toto je trochu zvláštní. Ten Object je co? Proč tady rovnou není List<Object>? V algoritmech, co to používáte, vám přijdou z monga instance Document, a ty pak rozbalujete do mapy. Ale už je neprocházíte rekurzivně. Jaký tedy má smysl rozbalit je do první úrovně, ale ne do druhé? Spíš bych tedy použil existující implementaci a rovnou tady měl něco jako List<Document>, nebo něco takového. Popřípadě si můžete vytvořit vlastní struktury (mapa, list a hodnota, viz ResultNode a třídy vedle ní).
    private List<Map<String, Object>> data;

    public DocumentResponse(List<Map<String, Object>> data, int itemCount, Set<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }
}
