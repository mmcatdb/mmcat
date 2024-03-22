package cz.matfyz.server.entity.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.instance.SuperIdWithValues;
import cz.matfyz.server.entity.instance.InstanceCategoryWrapper.WrapperContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author jachym.bartik
 */
public record InstanceObjectWrapper(
    Key key,
    SignatureId superId,
    List<DomainRowWrapper> rows
) {

    public static InstanceObjectWrapper fromInstanceObject(InstanceObject object, WrapperContext context) {
        final Map<DomainRow, Integer> rowToId = new TreeMap<>();
        context.rowToId.put(object.key(), rowToId);

        int lastId = 0;
        List<DomainRowWrapper> rows = new ArrayList<>();

        for (final DomainRow row : object.allRowsToSet()) {
            final var rowWrapper = new DomainRowWrapper(
                lastId++,
                row.superId,
                row.technicalIds.stream().toList(),
                row.pendingReferences.stream().toList()
            );
            rowToId.put(row, rowWrapper.id());
            rows.add(rowWrapper);
        }

        return new InstanceObjectWrapper(
            object.key(),
            object.superId(),
            rows
        );
    }

    public void toInstanceObject(WrapperContext context) {
        final InstanceObject object = context.category.getObject(key);

        final List<DomainRow> idToRow = new ArrayList<>();
        context.idToRow.put(key, idToRow);

        for (final DomainRowWrapper rowWrapper : rows) {
            final var row = new DomainRow(
                rowWrapper.superId,
                new TreeSet<>(rowWrapper.technicalIds),
                new TreeSet<>(rowWrapper.pendingReferences)
            );

            idToRow.set(rowWrapper.id, row);
            final var ids = row.superId.findAllIds(object.ids()).foundIds();
            object.setRow(row, ids);
        }
    }

    public record DomainRowWrapper(
        int id,
        SuperIdWithValues superId,
        List<String> technicalIds,
        List<Signature> pendingReferences
    ) {}
    
}
