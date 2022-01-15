package cz.cuni.matfyz.wrapperDummy;

import cz.cuni.matfyz.statements.ICStatement;

/**
 *
 * @author jachymb.bartik
 */
public class DummyICStatement implements ICStatement
{
    private final String content;
    
    public DummyICStatement(String content)
    {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}