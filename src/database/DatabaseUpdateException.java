package database;

public class DatabaseUpdateException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DatabaseUpdateException(String message)
	{
		super(message);
	}
	
	public DatabaseUpdateException(String message, Exception e)
	{
		super(message, e);
	}
}
