/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.category;

/**
 *
 * @author pavel.contos
 */
public class RelationshipObject implements CategoricalObject {

	private final String relationshipName;

	public RelationshipObject(String relationshipName) {
		this.relationshipName = relationshipName;
	}

	@Override
	public void add(Object object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String getName() {
		return relationshipName;
	}

    @Override
    public int size() {
        return -1;
    }

}
