package components;
import java.util.*;

public class Cart {
	
	private ArrayList<String> cartItems = new ArrayList<String>();
	
	private Cart() {
		//Nothing to do
	}
	
	public static Cart newCart() {
		return new Cart();
	}
	
	public void additem(String item) {
		cartItems.add(item);
	}
	
	public ArrayList<String> getItem(){
		return cartItems;
	}

}
