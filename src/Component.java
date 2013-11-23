import java.io.ObjectInputStream.GetField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.org.glassfish.external.statistics.annotations.Reset;

public class Component {
	/**
	 * Retrieve a client bank account's balance in each day.
	 * 
	 * @return a Hashtable<Day, Balance>
	 * @throws SQLException
	 */
	public Hashtable<String, Double> getSoldEachDay() throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT [price],[quantity], [transactionType],convert(varchar(10), [date],120) As Date FROM [dbo].[transactions]  order by Date ";
		ResultSet rset = stmt.executeQuery(query);
		Hashtable<String, Double> soldTable = new Hashtable<String, Double>();
		double sold = 0;
		while (rset.next()) {
			String date = rset.getString("Date");
			double price = rset.getDouble("price");
			int quantity = rset.getInt("quantity");
			String tranType = rset.getString("transactionType");
			if (tranType.equals("Buy")) {
				sold -= price * quantity;
			} else {
				sold += price * quantity;
			}
			soldTable.put(date, sold);
		}
		return soldTable;

	}

	/**
	 * Update the "Fait_Financier" table by setting the client balance in each
	 * day
	 * 
	 * @param soldTable
	 *            a Hashtable<Day, Balance>
	 * @throws SQLException
	 */
	public void updateSoldEachDay(Hashtable<String, Double> soldTable)
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		Enumeration<String> keys = soldTable.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			System.out.println(" Key :" + key + " Value: " + soldTable.get(key)
					+ "\n");
			String query = "UPDATE [dbo].[fait_financier] SET [solde] = "
					+ soldTable.get(key) + "  WHERE [date] = {d '" + key + "'}";
			stmt.executeUpdate(query);
		}
	}

	/**
	 * Retrieve buying transactions along with pairs of price and quantity
	 * 
	 * @throws SQLException
	 * @return Hashtable<ProductID, ArrayList<ArrayList<Price and Quantity>>>
	 */
	public Hashtable<String, ArrayList<ArrayList<Double>>> getBuyingTransaction()
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT  [productID], [price],[quantity] FROM [dbo].[transactions]  WHERE transactionType = 'Buy' order by Date";
		ResultSet rBuyTran = stmt.executeQuery(query);
		Hashtable<String, ArrayList<ArrayList<Double>>> buyTable = new Hashtable<String, ArrayList<ArrayList<Double>>>();
		while (rBuyTran.next()) {
			String productID = rBuyTran.getString("productID");
			double price = rBuyTran.getDouble("price");
			int quantity = rBuyTran.getInt("quantity");
			// Pairs of price and product.
			ArrayList<Double> priceQuantity = new ArrayList<Double>();
			priceQuantity.add(price);
			priceQuantity.add((double) quantity);
			// If the productID doesn't exist in the list
			if (!buyTable.containsKey(productID)) {
				ArrayList<ArrayList<Double>> productTran = new ArrayList<ArrayList<Double>>();
				productTran.add(priceQuantity);
				buyTable.put(productID, productTran);
			} else {
				ArrayList<ArrayList<Double>> productTran = buyTable
						.get(productID);
				productTran.add(priceQuantity);
			}
		}
		return buyTable;
	}

	/**
	 * Retrieve all selling date.
	 * 
	 * @return a ArrayList<String of Date>
	 * @throws SQLException
	 */
	public ArrayList<String> getSellingDate() throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT convert(varchar(10), [date],120) As Date FROM [dbo].[transactions]  order by Date ";
		ResultSet rset = stmt.executeQuery(query);
		ArrayList<String> sellingDate = new ArrayList<String>();
		double sold = 0;
		while (rset.next()) {
			String date = rset.getString("Date");
			if (!sellingDate.contains(date))
				sellingDate.add(date);
		}
		rset.close();
		stmt.close();
		return sellingDate;

	}

	public void calculateBenefitEachProduct(
			Hashtable<String, ArrayList<ArrayList<Double>>> buyTable,
			ArrayList<String> sellingDate) throws SQLException {
		// Query selling transactions
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		for (int index = 0; index < sellingDate.size(); index++) {
			String query = "SELECT  [productID], [price],[quantity] FROM [dbo].[transactions]  WHERE transactionType = 'Sell' AND [date] = {d '"
					+ sellingDate.get(index) + "'} ORDER By [date]";
			ResultSet rSellTran = stmt.executeQuery(query);
			Hashtable<String, Double> benefitEachProduct = new Hashtable<String, Double>();
			while (rSellTran.next()) {
				String productID = rSellTran.getString("productID");
				double price = rSellTran.getDouble("price");
				int quantity = rSellTran.getInt("quantity");
				ArrayList<ArrayList<Double>> productTran = buyTable
						.get(productID);
				// Initialize benefit product = 0
				if (!benefitEachProduct.containsKey(productID)) {
					benefitEachProduct.put(productID, 0.0);
				}
				double benefit = benefitEachProduct.get(productID);
				while (quantity != 0) {
					ArrayList<Double> priceQuantity = productTran.remove(0);
					// Quantity of stock less than or equal quantityTransaction
					if (priceQuantity.get(1) <= quantity) {
						benefit += price * priceQuantity.get(1)
								- priceQuantity.get(0) * priceQuantity.get(1);
						quantity -= priceQuantity.get(1);
					} else {
						benefit += price * quantity - priceQuantity.get(0)
								* quantity;
						priceQuantity.set(1, priceQuantity.get(1) - quantity);
						productTran.add(0, priceQuantity);
						quantity = 0;
					}
					benefitEachProduct.put(productID, benefit);
				}
			}
			// Update [fait_benefice]
			this.updatebenefitEachProduct(benefitEachProduct,
					sellingDate.get(index));
		}
		stmt.close();

	}

	/**
	 * Update the "Fait_Benefit" table by setting the benefit in each product
	 * 
	 * @param benefitEachProduct
	 *            a Hashtable<Product, benefit>
	 * @throws SQLException
	 */
	public void updatebenefitEachProduct(
			Hashtable<String, Double> benefitEachProduct, String date)
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		Enumeration<String> keys = benefitEachProduct.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			String query = "UPDATE [dbo].[fait_benefice] SET [benefice] = "
					+ benefitEachProduct.get(key) + "  WHERE [produitID] = '"
					+ key + "' AND [date] = {d '" + date + "'} ";
			stmt.executeUpdate(query);
			System.out.println(query);
		}
		stmt.close();
	}

	/**
	 * Get the quantity of Stock
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, Hashtable<String, Integer>> getQuantityStock()
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT [produitID],convert(varchar(10), [date],120) As Date,[variationStock] FROM [groupe6].[dbo].[stocks] ORDER BY date";
		ResultSet rset = stmt.executeQuery(query);
		Hashtable<String, Hashtable<String, Integer>> productTable = new Hashtable<String, Hashtable<String, Integer>>();
		Hashtable<String, Integer> productPreviousQuantity = new Hashtable<String, Integer>();

		while (rset.next()) {
			String date = rset.getString("Date");
			int variationStock = rset.getInt("variationStock");
			String product = rset.getString("produitID");
			// Initialize the previous quantity, if it doesn't exist
			if (!productPreviousQuantity.containsKey(product)) {
				productPreviousQuantity.put(product, 0);
			}
			// Update quantity of Stock in each product
			productPreviousQuantity.put(product,
					productPreviousQuantity.get(product) + variationStock);
			// Put the products in the Hashtable, if it doesn't exist
			if (!productTable.containsKey(product)) {
				productTable.put(product, new Hashtable<String, Integer>());
			}
			productTable.get(product).put(date,
					productPreviousQuantity.get(product));
		}
		rset.close();
		stmt.close();
		return productTable;
	}

	public void updateQuantityStock(
			Hashtable<String, Hashtable<String, Integer>> productTable)
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		Enumeration<String> productKeys = productTable.keys();
		while (productKeys.hasMoreElements()) {
			Object eachProductKey = productKeys.nextElement();
			Enumeration<String> dateKeys = productTable.get(eachProductKey)
					.keys();
			while (dateKeys.hasMoreElements()) {
				Object eachDateKey = dateKeys.nextElement();
				String query = "UPDATE [dbo].[stocks] SET [Quantite_disponible] = "
						+ productTable.get(eachProductKey).get(eachDateKey)
						+ " WHERE [produitID] = '"
						+ eachProductKey
						+ "' AND [date] = {d '" + eachDateKey + "'} ";
				stmt.executeUpdate(query);
				System.out.println(query);
			}
		}
		stmt.close();
	}

	/**
	 * Get value of stock (QuantityStock*LastSellingPrice) in each day
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Hashtable<String, Double> getValueOfStock() throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT [produitID],convert(varchar(10), [date],120) As Date, [dernierPrixVente],[Quantite_disponible]  FROM [dbo].[stocks]  order by Date ";
		ResultSet rset = stmt.executeQuery(query);
		Hashtable<String, Double> dateValueStock = new Hashtable<String, Double>();
		Hashtable<String, Double> productValueStock = new Hashtable<String, Double>();
		double previousLineValueStock = 0.0;
		String previousDate = "";
		while (rset.next()) {
			String product = rset.getString("produitID");
			String date = rset.getString("Date");
			double lastPrice = rset.getDouble("dernierPrixVente");
			int quantity = rset.getInt("quantite_disponible");
			double valueOfStock = lastPrice * quantity  +previousLineValueStock;
			// Initialize the Hashtable, dateValueStock
			if (!dateValueStock.containsKey(date)) {
				dateValueStock.put(date, 0.0);
			}
			// If the product has calculated before
			if(productValueStock.containsKey(product)){
				dateValueStock.put(date,  valueOfStock - productValueStock.get(product));
				// Keep value of the previous line
				previousLineValueStock = valueOfStock-productValueStock.get(product);
			}
			else{
				dateValueStock.put(date, valueOfStock);
				// Keep value of the previous line
				previousLineValueStock = valueOfStock;
			}
			// Keep last date
			previousDate = date;
			// Keep last selling for distinct products
			productValueStock.put(product, lastPrice * quantity);
		}
		rset.close();
		stmt.close();
		return dateValueStock;

	}

	/**
	 * Update value of Stock
	 * 
	 * @param dateValueStock
	 * @throws SQLException
	 */
	public void updateValueOfStock(Hashtable<String, Double> dateValueStock)
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		Enumeration<String> keys = dateValueStock.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			String query = "UPDATE [groupe6].[dbo].[fait_financier] SET [valeurStock] = "
					+ dateValueStock.get(key) + "  WHERE [date] = {d '" + key
					+ "'}";
			stmt.executeUpdate(query);
			System.out.println(query);
		}
		stmt.close();
	}

	public Hashtable<String, Hashtable<String, Double>> getLastSellingPrice()
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT [produitID],convert(varchar(10), [date],120) As Date FROM [groupe6].[dbo].[stocks] WHERE [dernierPrixVente] is NULL ORDER BY Date,[produitID]";
		ResultSet rset = stmt.executeQuery(query);
		Hashtable<String,ArrayList<String>> productDateNull = new Hashtable<String, ArrayList<String>>();
		Hashtable<String,ArrayList<String>> productNoPreviousDate = new Hashtable<String,ArrayList<String>>();
		Hashtable<String, Hashtable<String, Double>> productDatePrice = new Hashtable<String, Hashtable<String, Double>>();
		// Get all Products having NULL
		while (rset.next()) {
			String product = rset.getString("produitID");
			String date = rset.getString("Date");
			// Initialize productDateNull
			if(!productDateNull.containsKey(product)){
				productDateNull.put(product, new ArrayList<String>());
			} 
			productDateNull.get(product).add(date);
		}
		
		// Loops all the products in the Hashtable(ProductID,ArrayList(Date))
		Enumeration<String> productKeys = productDateNull.keys();
		while (productKeys.hasMoreElements()) {
			Object eachProductKey = productKeys.nextElement();
			for(int i=0;i<productDateNull.get(eachProductKey).size();i++){
			// Simplify Date by removing '-'
			// If there is a previous date
			query = "SELECT [quantity],[price] FROM [groupe6].[dbo].[transactions] WHERE [productID] = "
					+ eachProductKey
					+ " AND [date] < {d '"
					+ productDateNull.get(eachProductKey).get(i)
					+ "'} AND [transactionType] = 'Sell' ORDER BY [date] DESC";
			rset = stmt.executeQuery(query);
			// Verify whether there are any results or not. In other words, are
			// there any previous dates? If yes!
			if (rset.next()) {
				if(!productDatePrice.containsKey(eachProductKey)){
					productDatePrice.put((String) eachProductKey, new Hashtable<String,Double>());
				}
				productDatePrice.get(eachProductKey).put(productDateNull.get(eachProductKey).get(i), rset.getDouble("price"));
			} else {
				if(!productNoPreviousDate.containsKey(eachProductKey)){
					productNoPreviousDate.put((String) eachProductKey,new ArrayList<String>());
				}
				productNoPreviousDate.get(eachProductKey).add(productDateNull.get(eachProductKey).get(i));
			}
			}

		}
		// Loops all the products no previous value
		int totalQuantity;
		double totalPrice;
		productKeys = productNoPreviousDate.keys();
		while (productKeys.hasMoreElements()) {
			Object eachProductKey = productKeys.nextElement();
			for(int i=0;i<productNoPreviousDate.get(eachProductKey).size();i++){
			query = "SELECT [quantity],[price] FROM [groupe6].[dbo].[transactions] WHERE [productID] = "
					+ eachProductKey + " AND [date] = {d '" + productNoPreviousDate.get(eachProductKey).get(i) + "'}";
			rset = stmt.executeQuery(query);
			totalQuantity = 0;
			totalPrice = 0.0;
			while (rset.next()) {
				int quantity = rset.getInt("quantity");
				double price = rset.getDouble("price");
				totalQuantity += quantity;
				totalPrice += price*quantity;
			}
			if(!productDatePrice.containsKey(eachProductKey)){
				productDatePrice.put((String) eachProductKey, new Hashtable<String,Double>());
			}
			productDatePrice.get(eachProductKey).put(productNoPreviousDate.get(eachProductKey).get(i), totalPrice/totalQuantity);
		}
		}
		return productDatePrice;
	}

	
	public void updateLastSellingPrice(Hashtable<String, Hashtable<String, Double>> productDatePrice)
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		Enumeration<String> productKeys = productDatePrice.keys();

		while (productKeys.hasMoreElements()) {
			Object eachProductkey = productKeys.nextElement();
			Enumeration<String> dateKeys = productDatePrice.get(eachProductkey).keys();
			while (dateKeys.hasMoreElements()) {
				Object eachDatekey = dateKeys.nextElement();
			String query = "UPDATE [groupe6].[dbo].[stocks] SET [dernierPrixVente] = "
					+ productDatePrice.get(eachProductkey).get(eachDatekey) + "  WHERE [produitID] = '"
					+ eachProductkey + "' AND [date] = {d '" + eachDatekey + "'} ";
			
			stmt.executeUpdate(query);
			System.out.println(query);
			}
		}
		stmt.close();
	}
	
	public static void main(String[] args) throws SQLException {
		Component c = new Component();

		// Calculate balance in each day.
		// Hashtable<String, Double> soldeTable = new Hashtable<String,
		// Double>();
		// soldeTable = c.getSoldEachDay();
		// c.updateSoldEachDay(soldeTable);

		// Calculate benefit
		// Hashtable<String, ArrayList<ArrayList<Double>>> buyTable = c
		// .getBuyingTransaction();
		// ArrayList<String> sellingDate = c.getSellingDate();
		// c.calculateBenefitEachProduct(buyTable, sellingDate);

		// Calculate Quantity of Stock
		// Hashtable<String, Hashtable<String, Integer>> productTable = new
		// Hashtable<String, Hashtable<String, Integer>>();
		// productTable = c.getQuantityStock();
		// c.updateQuantityStock(productTable);

		// Calculate value of Stock
		 Hashtable<String, Double> dateValueStock = new Hashtable<String,Double>();
		 dateValueStock = c.getValueOfStock();
		 c.updateValueOfStock(dateValueStock);
		
		// Replace NULL values in last selling price
//		Hashtable<String, Hashtable<String, Double>> productDatePrice = new Hashtable<String, Hashtable<String, Double>>();
//		productDatePrice = c.getLastSellingPrice();
//		c.updateLastSellingPrice(productDatePrice);

	}

}
