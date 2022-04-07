package cz.cuni.matfyz.server.view;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceObject;

/**
 * 
 * @author jachym.bartik
 */
public class InstanceObjectView {

    public KeyView key;
    public List<SignatureView> columns;
    public List<List<String>> rows;

    public InstanceObjectView(InstanceObject instanceObject) {
        this.key = new KeyView(instanceObject.key());

        var originalRows = instanceObject.rows();

        var signatures = new TreeSet<Signature>();
        originalRows.forEach(row -> signatures.addAll(row.signatures()));
        this.columns = signatures.stream().map(signature -> new SignatureView(signature)).toList();

        this.rows = new ArrayList<>();
        originalRows.forEach(row -> {
            var newRow = signatures.stream().map(signature -> row.getValue(signature)).toList();
            rows.add(newRow);
        });
    }

}
