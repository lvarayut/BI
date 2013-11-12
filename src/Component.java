import java.io.ObjectInputStream.GetField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class Component {
	/**
	 * Composant logiciel avec des exemples
	 */
	public class ComposantBD {

		/**
		 * RÃ©cupÃ©ration de la liste complÃ¨te des livres triÃ©e par titre.
		 * 
		 * @return un <code>ArrayList</code> contenant autant de tableaux de
		 *         String (5 chaÃ®nes de caractÃ¨res) que de livres dans la
		 *         base.
		 * @throws SQLException
		 *             en cas d'erreur de connexion Ã la base.
		 */
		// public static ArrayList<String[]> listeTousLivres() throws
		// SQLException {
		//
		// ArrayList<String[]> livres = new ArrayList<String[]>();
		//
		// Statement stmt =
		// DatabaseConnection.getConnection().createStatement();
		// String query = "select * from livre order by titre";
		// ResultSet rset = stmt.executeQuery(query);
		//
		// while (rset.next()) {
		// String[] livre = new String[5];
		// livre[0] = rset.getString("id");
		// livre[1] = rset.getString("isbn10");
		// livre[2] = rset.getString("isbn13");
		// livre[3] = rset.getString("titre");
		// livre[4] = rset.getString("auteur");
		//
		// livres.add(livre);
		// }
		// rset.close();
		// stmt.close();
		//
		// return livres;
		// }
		//
		//
		// /**
		// * RÃ©fÃ©rencement d'un nouveau livre dans la base de donnÃ©es.
		// * @param isbn10
		// * @param isbn13
		// * @param titre
		// * @param auteur
		// * @return l'identifiant (id) du livre crÃ©Ã©.
		// * @throws SQLException en cas d'erreur de connexion Ã la base.
		// */
		// public static int creerLivre(String isbn10, String isbn13, String
		// titre, String auteur) throws SQLException {
		// Statement stmt =
		// DatabaseConnection.getConnection().createStatement();
		//
		// String insert =
		// "insert into livre values(nextval('livre_id_seq'), '"+isbn10+"','"+isbn13+"','"+titre+"','"+auteur+"')";
		// stmt.executeUpdate(insert);
		//
		// String query =
		// "select currval('livre_id_seq') as valeur_courante_id_livre";
		// ResultSet rset = stmt.executeQuery(query);
		// rset.next();
		// int id = rset.getInt("valeur_courante_id_livre");
		// rset.close();
		// stmt.close();
		//
		// return id;
		// }
	}

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
				String query = "UPDATE [dbo].[stocks] SET [Quantité_disponible] = "
						+ productTable.get(eachProductKey).get(eachDateKey)
						+ " WHERE [produitID] = '"
						+ eachProductKey
						+ "' AND [date] = {d '" + eachDateKey + "'} ";
				stmt.executeUpdate(query);
				System.out.println(query);
			}
		}
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
		Hashtable<String, Hashtable<String, Integer>> productTable = new Hashtable<String, Hashtable<String, Integer>>();
		productTable = c.getQuantityStock();
		c.updateQuantityStock(productTable);

	}

}
