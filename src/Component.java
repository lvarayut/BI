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
	 * @return a Hashtable<Day, Balance>
	 * @throws SQLException
	 */
	public Hashtable<String, Float> getSoldEachDay() throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT [price],[quantity], [transactionType],convert(varchar(10), [date],120) As Date FROM [dbo].[transactions]  order by Date ";
		ResultSet rset = stmt.executeQuery(query);
		Hashtable<String, Float> soldTable = new Hashtable<String, Float>();
		float sold = 0;
		while (rset.next()) {
			String date = rset.getString("Date");
			float price = rset.getFloat("price");
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
	 * Update the "Fait_Financier" table 
	 * by setting the client balance in each day
	 * @param soldTable a Hashtable<Day, Balance>
	 * @throws SQLException
	 */
	public void updateSoldEachDay(Hashtable<String, Float> soldTable)
			throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		Enumeration keys = soldTable.keys();
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
	 * @throws SQLException
	 */
	public void getBuyTransaction() throws SQLException {
		Statement stmt = DatabaseConnection.getConnection().createStatement();
		String query = "SELECT  [productID], [price],[quantity] FROM [dbo].[transactions]  WHERE transactionType = 'Buy' order by Date";
		ResultSet rBuyTran = stmt.executeQuery(query);
		Hashtable<String, Float> buyTable = new Hashtable<String, Float>();
	}

	public static void main(String[] args) throws SQLException {
		Component c = new Component();
		Hashtable<String, Float> soldeTable = new Hashtable<String, Float>();
		soldeTable = c.getSoldEachDay();
		c.updateSoldEachDay(soldeTable);

	}

}
