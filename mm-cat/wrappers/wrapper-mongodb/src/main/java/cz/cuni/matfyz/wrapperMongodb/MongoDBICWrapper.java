/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.core.mapping.IdentifierStructure;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.core.util.Pair;
import cz.cuni.matfyz.statements.ICStatement;
import java.util.Set;

/**
 *
 * @author jachym.bartik
 */
public class MongoDBICWrapper implements AbstractICWrapper
{
	public void appendIdentifier(String kindName, IdentifierStructure identifier) {}

	public void appendReference(String kindName, String kindName2, Set<Pair<String, String>> attributePairs) {}

	public ICStatement createICStatement()
    {
        return new MongoDBICStatement("");
    }

	public ICStatement createICRemoveStatement()
    {
        return new MongoDBICStatement("");
    }
}

class MongoDBICStatement implements ICStatement
{
    private String content;
    
    public MongoDBICStatement(String content) {
        this.content = content;
    }
}
