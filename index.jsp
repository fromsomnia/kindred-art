<%@ page language="java" 
	contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.*"
    import="components.MerchandiseObject"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
List<MerchandiseObject> inventory = MerchandiseObject.getAllMerchandise();
%>
<title>Kindred-Art.com</title>
</head>
<body>

Current Inventory:<br>
<br>
<%
int invSize = inventory.size();
for(int i = 0; i < invSize; i++)
{
	out.print("Inv Num: " + inventory.get(i).getID() + "<br>");
	out.print("Price  : " + inventory.get(i).getPrice() + "<br><br>");
}
%>

</body>
</html>