package cz.cuni.matfyz.server.entity;

import java.io.Serializable;

/**
 * 
 * @author jachym.bartik
 */
public class Task implements Serializable
{
    public Type type;

    public enum Type
    {
        DatabaseToDatabase
    }
}