package database;

public class DatabaseReadException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DatabaseReadException(String message)
	{
		super(message);
	}
	
	public DatabaseReadException(String message, Exception e)
	{
		super(message, e);
	}
}
