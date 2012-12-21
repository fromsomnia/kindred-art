package database;

import static database.DatabaseConstants.*;

/**
 * Handles all database requests
 *
 */
public class DatabaseManager
{
	private static DatabaseConnection _dbConnection = null;
	
	private DatabaseManager()
	{
	}
	
	public static DatabaseConnection getInstance() throws DatabaseConnectionException
	{
		if(_dbConnection == null || _dbConnection.isClosed())
		{
			_dbConnection = null;
			try {
				Class.forName(DB_DRIVER);
			} catch (ClassNotFoundException e) {
				throw new DatabaseConnectionException("MySQL Driver could not be loaded.");
			}
			
			_dbConnection = new DatabaseConnection();
		}
		
		return _dbConnection;
	}
	
	public static void shutdown()
	{
		if(_dbConnection != null)
		{
			_dbConnection.close();
		}
		
		_dbConnection = null;
	}
	
	public static DatabaseConnection restart() throws DatabaseConnectionException
	{
		shutdown();
		return getInstance();
	}
}
