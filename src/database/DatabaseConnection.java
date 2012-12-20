package database;

import java.sql.*;
import java.util.*;


public class DatabaseConnection
{
	public enum Compare { EQUAL, GREATER, LESS, GE, LE, LIKE, NULL, NOCOMPARE, CONTENT };
	
	private Connection connection;
	private boolean closed;
	
	public DatabaseConnection() throws DatabaseConnectionException
	{
		Properties props = new Properties();
		
		props.setProperty("user", DatabaseConstants.DB_USER);
		props.setProperty("password", DatabaseConstants.DB_PASSWORD);
		
		String uri = "jdbc:mysql://" + DatabaseConstants.DB_HOST + "/" + DatabaseConstants.DB_NAME;
		
		try {
			connection = DriverManager.getConnection(uri, props);
		} catch (SQLException e) {
			throw new DatabaseConnectionException("Unable to connect to database", e);
		}
		
		this.closed = false;
	}
	
	public void close()
	{
		close(null, null);
	}
	
	private void close(Statement stmt)
	{
		close(stmt, null);
	}
	
	private void close(Statement stmt, ResultSet rSet)
	{
		try {
			if(rSet != null)
				rSet.close();
		} catch(SQLException ignored) {}
		rSet = null;
		
		try {
			if(stmt != null)
				stmt.close();
		} catch(SQLException ignored) {}
		stmt = null;
		
		try {
			connection.close();
		} catch(SQLException ignored) {}
		
		this.closed = true;
	}
	
	public boolean isClosed()
	{
		return this.closed;
	}
	
	@Override
	public void finalize() throws Throwable
	{
		this.close();
		super.finalize();
	}
	
	public List<Map<String, String>> getFieldsFromRecords(String table, String known_col, String known_val, Compare cmp, String... fields)
	{
		return getFieldsFromRecords(table, known_col, known_val, cmp, null, null, Compare.NULL, fields);
	}
	
	public List<Map<String, String>> getFieldsFromRecords(String table, String known_col_1, String known_val_1, Compare cmp1, String known_col_2, String known_val_2, Compare cmp2, String... fields)
	{
		String query = "SELECT " + sqlizeFields(fields) + " FROM " + table + sqlizeCompare(cmp1, known_col_1, cmp2, known_col_2) + ";";
		List<Map<String, String>> matchingRecords = new ArrayList<Map<String, String>>();
		PreparedStatement stmt = null;
		ResultSet rSet = null;
		try {
			stmt = connection.prepareStatement(query);
			
			if((cmp1 == Compare.NULL) || (cmp1 == Compare.NOCOMPARE))
			{
				if((cmp2 != Compare.NULL) && (cmp2 != Compare.NOCOMPARE))
					stmt.setString(1,  known_val_2);
			}
			else
			{
				stmt.setString(1, known_val_1);
				if((cmp2 != Compare.NULL) && (cmp2 != Compare.NOCOMPARE))
					stmt.setString(2,  known_val_2);
			}
			
			
			rSet = stmt.executeQuery();
			
			while(rSet.next())
			{
				matchingRecords.add(queryToMap(rSet));
			}
			return matchingRecords;
		} catch(SQLException e) {
			this.close(stmt, rSet);
			stmt = null;
			rSet = null;
			throw new DatabaseReadException("Error while reading from database", e);
		} finally {
			try {
				if(rSet != null)
					rSet.close();
			} catch(SQLException ignored) {}
			try {
				if(stmt != null)
					stmt.close();
			} catch(SQLException ignored) {}
		}
	}
	
	public String getFieldFromRecord(String table, String pkey_col, String pkey, String field)
	{
		List<Map<String, String>> list = getFieldsFromRecords(table, pkey_col, pkey, Compare.EQUAL, field);
		if((list == null) || (list.size() == 0))
			return null;
		Map<String, String> map = list.get(0);
		return map.get(field);
	}
	
	public List<String> getFieldFromMatchingRecords(String table, String col, String col_value, Compare cmp, String field)
	{
		List<String> result = new ArrayList<String>();
		
		List<Map<String, String>> list = getFieldsFromRecords(table, col, col_value, cmp, field);
		
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				result.add(list.get(i).get(field));
			}
		}
		
		return result;
	}
	
	public Map<String, String> getSingleRecord(String table, String column, String value)
	{
		List<Map<String, String>> list = getFieldsFromRecords(table, column, value, Compare.EQUAL);
		if((list == null) || (list.size() == 0))
			return null;
		Map<String, String> map = list.get(0);
		return map;
	}
	
	public Map<String, String> getSingleRecord(String table, String column, String value, String field, String... fields)
	{
		String[] allFields = new String[fields.length + 1];
		allFields[0] = field;
		for(int i = 0; i < fields.length; i++)
			allFields[i + 1] = fields[i];
		List<Map<String, String>> list = getFieldsFromRecords(table, column, value, Compare.EQUAL, allFields);
		if((list == null) || (list.size() == 0))
			return null;
		Map<String, String> map = list.get(0);
		return map;
	}
	
	public List<Map<String, String>> getMatchingRecords(String table, String col, String val)
	{
		return getFieldsFromRecords(table, col, val, Compare.EQUAL);
	}
	
	public List<Map<String, String>> getAllRecords(String table)
	{
		String query = "SELECT * FROM " + table + ";";
		List<Map<String, String>> matchingRecords = new ArrayList<Map<String, String>>();
		PreparedStatement stmt = null;
		ResultSet rSet = null;
		
		try {
			stmt = connection.prepareStatement(query);
			
			rSet = stmt.executeQuery();
			
			while(rSet.next())
			{
				matchingRecords.add(queryToMap(rSet));
			}
		} catch(SQLException e) {
			// stop processing the results if an exception is thrown
			// and just return what has already been processed
		} finally {
			try {
				if(rSet != null)
					rSet.close();
			} catch(SQLException ignored) {}
			try {
				if(stmt != null)
					stmt.close();
			} catch(SQLException ignored) {}
		}
		return matchingRecords;
	}
	
	public boolean readComponent(IStorable component, String pkey)
	{
		Map<String, String> data = getSingleRecord(component.getTableName(), component.getColumnNames()[0], pkey);
		if(data == null)
			return false;
		component.loadComponent(data);
		return true;
	}

	public void writeComponent(IStorable component)
	{
		PreparedStatement stmt = null;
		
		try {
			Map<String, String> data = component.storeComponent();
			String[] columns = data.keySet().toArray(new String[0]);
			String query = "INSERT INTO " + component.getTableName();
			query += createSetQuery(data, columns) + ";";
			stmt = connection.prepareStatement(query);
			
			int c = 1;
			for(int i = 0; i < columns.length; i++)
			{
				if(data.containsKey(columns[i]) && !data.get(columns[i]).isEmpty())
				{
					stmt.setString(c, data.get(columns[i]));
					c++;
				}
			}
			stmt.executeUpdate();
		} catch(SQLException ex) {
			this.close(stmt);
			stmt = null;
			throw new DatabaseWriteException("Failed to write component to database. Reason: " + ex.getMessage(), ex);
		} finally {
			try {
				if(stmt != null)
					stmt.close();
			} catch (SQLException ignored) {}
		}
	}
	
	public void updateComponent(IStorable component)
	{
		String[] columns = component.getColumnNames();
		String[] remainingColumns = new String[columns.length - 1];
		for(int i = 1; i < columns.length; i++)
			remainingColumns[i - 1] = columns[i];
		Map<String, String> data = component.storeComponent();
		String query = "UPDATE " + component.getTableName();
		query += createSetQuery(data, remainingColumns);
		query += " WHERE " + columns[0] + "=?";
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(query);
			
			int c = 1;
			for(int i = 1; i < columns.length; i++)
			{
				if(data.containsKey(columns[i]) && !data.get(columns[i]).isEmpty())
				{
					stmt.setString(c, data.get(columns[i]));
					c++;
				}
			}
			
			stmt.setString(c, component.getPrimaryKey());
			stmt.executeUpdate();
		} catch(SQLException ex) {
			this.close(stmt);
			stmt = null;
			throw new DatabaseUpdateException("Failed to update component in database", ex);
		} finally {
			try {
				if(stmt != null)
					stmt.close();
			} catch (SQLException ignored) {}
		}
	}
	
	/**
	 * Delete a component from the database.
	 * 
	 * @param component		The component object to remove from the table.
	 */
	public void deleteComponent(IStorable component)
	{
		if( component == null )
			return;
		
		//Map<String, String> values = component.storeComponent();
		//String pkey = values.get(component.getPrimaryKey());

		deleteComponent(component.getTableName(), component.getColumnNames()[0], component.getPrimaryKey());
	}
	
	/**
	 * Delete all matching items in the table.
	 * 
	 * @param table		The name of the table to search in
	 * @param column	The column to examine
	 * @param value		The value the column must have to be deleted.
	 */
	public void deleteComponent(String table, String column, String value)
	{
		String query = "DELETE FROM " + table + " WHERE " + column + " = ?;";
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(query);
			
			stmt.setString(1, value);
			stmt.executeUpdate();
		} catch(SQLException ex) {
			this.close(stmt);
			stmt = null;
			throw new DatabaseDeletionException("Failed to delete component from database", ex);
		} finally {
			try {
				if(stmt != null)
					stmt.close();
			} catch (SQLException ignored) {}
		}
	}
	
	private Map<String, String> queryToMap(ResultSet rSet) throws SQLException
	{
		int columns = 0;
		Map<String, String> result = null;
		
		columns = rSet.getMetaData().getColumnCount();
		result = new HashMap<String, String>(columns);
		for(int i = 1; i <= columns; i++)
		{
			String name = rSet.getMetaData().getColumnName(i);
			result.put(name, rSet.getString(i));
		}

		return result;
	}
	
	private String createSetQuery(Map<String, String> data, String... columns)
	{
		String result = " SET ";
		
		for(int i = 0; i < columns.length; i++)
		{
			if(data.containsKey(columns[i]) && !data.get(columns[i]).isEmpty())
			{
				result += columns[i] + " = ?";
				if((i + 1) < columns.length)
				{
					result += ", ";
				}
			}
		}
		
		return result;
	}
	
	private String sqlizeFields(String[] fields)
	{
		if((fields == null) || (fields.length == 0) || (fields[0] == null) || (fields[0].equals("")))
		{
			return "*";
		}
		
		String result = fields[0];
		for(int i = 1; i < fields.length; i++)
		{
			result += ", " + fields[i];
		}
		
		return result;
	}
	
	private String sqlizeCompare(Compare cmp, String col)
	{
		String firstPart = col + " ";
		String lastPart = " ?";
		switch(cmp)
		{
		case EQUAL:
			return firstPart + "=" + lastPart;
		case LESS:
			return firstPart + "<" + lastPart;
		case GREATER:
			return firstPart + ">" + lastPart;
		case LE:
			return firstPart + "<=" + lastPart;
		case GE:
			return firstPart + ">=" + lastPart;
		case LIKE:
			return firstPart + "LIKE" + lastPart;
		case CONTENT:
			return " WHERE UPPER(REPLACE(" + col + ", ' ', '')) LIKE UPPER(REPLACE(?, ' ', ''))";
		default:
			return "";
		}
	}
	
	private String sqlizeCompare(Compare cmp1, String col1, Compare cmp2, String col2)
	{
		String p1 = sqlizeCompare(cmp1, col1);
		String p2 = sqlizeCompare(cmp2, col2);
		String where = " WHERE ";
		if(p1.isEmpty())
		{
			if(p2.isEmpty())
			{
				return "";
			}
			else
			{
				return where + p1;
			}
		}
		else
		{
			where += p1;
			if(p2.isEmpty())
			{
				return where;
			}
			else
			{
				return where + " AND " + p2;
			}
		}
	}
}
