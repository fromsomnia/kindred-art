package database;

public class DatabaseWriteException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DatabaseWriteException(String message)
	{
		super(message);
	}
	
	public DatabaseWriteException(String message, Exception e)
	{
		super(message, e);
	}
}
