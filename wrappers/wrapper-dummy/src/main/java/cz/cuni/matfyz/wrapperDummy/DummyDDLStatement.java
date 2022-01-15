package cz.cuni.matfyz.wrapperDummy;

import cz.cuni.matfyz.statements.DDLStatement;

/**
 *
 * @author jachymb.bartik
 */
public class DummyDDLStatement implements DDLStatement {
    
    private final String content;
    
    public DummyDDLStatement(String content)
    {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}