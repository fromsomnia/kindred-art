package database;

import java.util.Map;

public interface IStorable
{
	public void loadComponent(Map<String, String> data);
	public Map<String, String> storeComponent();

	public String getPrimaryKey();
	public String getTableName();
	public String[] getColumnNames();
	public int[] getColumnTypes();
}

