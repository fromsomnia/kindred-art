package components;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import database.DatabaseConnection;
import database.DatabaseConnectionException;
import database.DatabaseManager;
import database.IStorable;

public class MerchandiseObject implements IStorable{
	
	public static final String TABLE = "inventory";
	public static final String PRIMARY_KEY = "inv_id";
	public static final String PRICE = "price";
	
	public static final String[] COLUMN_NAMES = {PRIMARY_KEY, PRICE};
	public static final int[] COLUMN_TYPES = {Types.INTEGER, Types.FLOAT};
	
	private static DatabaseConnection USER_DATABASE_;
	
	private int inv_id;
	private double price;
	
	/**
	 * Ensures that the database connection is valid.
	 */
	private static void checkConnection()
	{
		try
		{
			if( (USER_DATABASE_ == null) || USER_DATABASE_.isClosed() )
				USER_DATABASE_ = DatabaseManager.getInstance();
		}
		catch (DatabaseConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private MerchandiseObject() {
		//Nothing to do
	}


	@Override
	public void loadComponent(Map<String, String> data) {
		if( (data == null) || (data.size() != COLUMN_NAMES.length) )
			throw new IllegalArgumentException("Invalid number of columns.");

		inv_id = Integer.parseInt(data.get(PRIMARY_KEY));
		price = Double.parseDouble(data.get(PRICE));
	}


	@Override
	public Map<String, String> storeComponent() {
		HashMap<String, String> data = new HashMap<String, String>();
		
		data.put(PRIMARY_KEY, Integer.toString(inv_id));
		data.put(PRICE, Double.toString(price));
		return data;
	}


	@Override
	public String getPrimaryKey() {
		return PRIMARY_KEY;
	}


	@Override
	public String getTableName() {
		return TABLE;
	}


	@Override
	public String[] getColumnNames() {
		return COLUMN_NAMES;
	}


	@Override
	public int[] getColumnTypes() {
		return COLUMN_TYPES;
	}
}
