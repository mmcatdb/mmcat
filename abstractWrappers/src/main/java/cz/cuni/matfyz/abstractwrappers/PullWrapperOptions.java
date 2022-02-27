package cz.cuni.matfyz.abstractwrappers;

/**
 *
 * @author jachym.bartik
 */
public class PullWrapperOptions
{
	private String kindName = null;

	public String getKindName()
	{
		return kindName;
	}

	private String command = null;

	public String getCommand()
	{
		return command;
	}

	public boolean hasCommand()
	{
		return command != null;
	}
	
	private Integer offset = null;

	public int getOffset()
	{
		return offset;
	}

	public boolean hasOffset()
	{
		return offset != null;
	}

	private Integer limit = null;

	public int getLimit()
	{
		return limit;
	}

	public boolean hasLimit()
	{
		return limit != null;
	}

	public static class Builder
    {
        private PullWrapperOptions instance = new PullWrapperOptions();

        public Builder offset(int value)
        {
            instance.offset = value;
            return this;
        }

		public Builder limit(int offset)
        {
            instance.offset = offset;
            return this;
        }

        public PullWrapperOptions buildWithCommand(String command)
        {
            instance.command = command;
            return instance;
        }

		public PullWrapperOptions buildWithKindName(String kindName)
        {
            instance.kindName = kindName;
            return instance;
        }
    }
}
