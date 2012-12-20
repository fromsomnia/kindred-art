package components;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	//TODO Add check for merch exists
	
	public static MerchandiseObject createMerchandiseObject(int inv_num, double price_val) {
		if(inv_num <= 0 || price_val <= 0)
		throw new NullPointerException("One or more null objects in merchandise parameters.");
		
		checkConnection();
		
		MerchandiseObject merchandise = new MerchandiseObject();
		merchandise.setInvID(inv_num);
		merchandise.setPrice(price_val);
		
		USER_DATABASE_.writeComponent(merchandise);
		
		return merchandise;		
	}
	
	public static List<MerchandiseObject> getAllMerchandise()
	{
		checkConnection();
		
		List<Map<String, String> > allMerch = USER_DATABASE_.getAllRecords(TABLE);
		List<MerchandiseObject> inventory = new ArrayList<MerchandiseObject>();
		for(Map<String, String> data : allMerch) {
			MerchandiseObject merchandise = new MerchandiseObject();
			merchandise.loadComponent(data);
			inventory.add(merchandise);
		}
		return inventory;
	}
	
	public static MerchandiseObject getMerchandiseByID(int id) 
	{
		checkConnection();
		
		MerchandiseObject merchandise = new MerchandiseObject();
		USER_DATABASE_.readComponent(merchandise, Integer.toString(id));
		return merchandise;
		//TODO ADD fail check
	}
	
	private void setPrice(double price_val) {
		if(price_val <= 0)
			throw new NullPointerException("One or more null objects in merchandise parameters.");
		price = price_val;
	}
	
	private void setInvID(int inv_id_val) {
		if(inv_id_val <= 0)
			throw new NullPointerException("One or more null objects in merchandise parameters.");
		inv_id = inv_id_val;
	}
	
	public double getPrice() {
		return price;
	}
	
	public int getID() {
		return inv_id;
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
