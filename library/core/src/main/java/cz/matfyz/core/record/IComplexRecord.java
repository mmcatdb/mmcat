package cz.matfyz.core.record;

import cz.matfyz.core.identifiers.Signature;

import java.util.List;

/**
 * This class represents an inner node (so it can be root) of the record tree.
 * The value of this record are its children.
 * @author jachymb.bartik
 */
public interface IComplexRecord {

    RecordName name();

    boolean hasComplexRecords(Signature signature);

    List<? extends IComplexRecord> getComplexRecords(Signature signature);

    boolean hasDynamicNameChildren();

    List<? extends IComplexRecord> getDynamicNameChildren();

    Signature dynamicNameSignature();

    boolean hasSimpleRecord(Signature signature);

    SimpleRecord<?> getSimpleRecord(Signature signature);

    boolean hasDynamicNameValues();

    List<SimpleValueRecord<?>> getDynamicNameValues();

    boolean containsDynamicNameValue(Signature signature);

}
