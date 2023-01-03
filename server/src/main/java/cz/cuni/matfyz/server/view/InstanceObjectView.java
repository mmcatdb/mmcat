package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author jachym.bartik
 */
public class InstanceObjectView {

    public final KeyView key;
    public final List<SignatureView> columns;
    public final List<List<String>> rows;

    public InstanceObjectView(InstanceObject instanceObject) {
        this.key = new KeyView(instanceObject.key());

        var originalRows = instanceObject.allRowsToSet();

        var signatures = new TreeSet<Signature>();
        originalRows.forEach(row -> signatures.addAll(row.signatures()));
        this.columns = signatures.stream().map(SignatureView::new).toList();

        this.rows = new ArrayList<>();
        originalRows.forEach(row -> {
            var newRow = signatures.stream().map(row::getValue).toList();
            rows.add(newRow);
        });
    }

}
