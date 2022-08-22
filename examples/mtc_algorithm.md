# MTC

```java
final ForestOfRecords forest;
for (RootRecord rootRecord : forest)
    processRootRecord(rootRecord);

void processRootRecord(RootRecord rootRecord) {
    // preparation phase - with root object:
    // createStackWithObject()
    qI := InstI(rootκ)
    S := fetchSids(rootκ.superid, r, null)
    foreach sid in S do
    sid := modifyActiveDomain(qI, sid)
    foreach (context, value) in children(Pκ) do
    M.push((sid, context, value))

    // processing of the tree:
    while (!M.empty())
        processTopOfStack(M);
}

/*
M = {
    ActiveDomainRow pid;    // 
    SchemaMorphism mS;      // 
    AccessPath t;           // 
    IComplexRecord record;  // 
}
*/
void processTopOfStack(Stack<StackTriple> M) {
    //(pid, mS, t) := M.pop()
    StackTriple triple = M.pop();
    //mI := InstI(mS)
    InstanceMorphism morphismInstance = instance.getMorphism(triple.parentToChildMorphism);
    //oS := mS.cod
    SchemaObject childObject = triple.parentToChildMorphism.cod();
    //qI := InstI(oS)
    InstanceObject childInstance = instance.getObject(childObject);
    //S := fetchSids(oS.superid, r, pid)
    Iterable<FetchedSuperId> sids = fetchSids(triple.parentRecord, triple.parentRow, triple.parentToChildMorphism);

    // foreach sid in S do
    for (Pair<IdWithValues, IComplexRecord> sid : sids) {
        //sid := modifyActiveDomain(qI, sid)
        ActiveDomainRow childRow = modify(childInstance, sid.idWithValues);

        //addRelation(mI,pid, sid, r)
        addRelation(morphismInstance, triple.parentRow, childRow, sid.childRecord);
        //addRelation(m−1I,sid, pid, r)
        addRelation(morphismInstance.dual(), childRow, triple.parentRow, sid.childRecord);

        //foreach (context, value) in children(t) do
        //    M.push((sid, context, value))
        addPathChildrenToStack(M, triple.t, childRow, sid.childRecord);
    }
}

// Record by měl svojí strukturou přesně odpovídat access path

// Fetch id-with-values for given record.
// The output is a set of (Signature, String) for each Signature in superId and its corresponding value from record.
// Actually, there can be multiple values in the record, so a list of these sets is returned.
Iterable<Pair<IdWithValues, IComplexRecord>> fetchSids(Id superId, IComplexRecord parentRecord, ActiveDomainRow parentRow, SchemaMorphism morphism) {

}
```

- Dopočítání krátkých morfizmů z dlouhého $f \colon A \rightarrow B$ bude možné provést jen tehdy, když budou existovat $g_1 \colon A \rightarrow C$ a $g_2 \colon B \rightarrow C$ takové, že $f \equiv \overline{g_2} \circ g_1$ a zároveň jak $g_1$, tak $g_2$ mají kardinalitu $\texttt{?..?-?..1}$, Definujme, že $\equiv$ znamená, že dané dva morfizmy se skládají z přesně stejných základních morfizmů.