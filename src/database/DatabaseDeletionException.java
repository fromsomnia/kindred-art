package database;

public class DatabaseDeletionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DatabaseDeletionException(String message)
	{
		super(message);
	}
	
	public DatabaseDeletionException(String message, Exception e)
	{
		super(message, e);
	}
}
