import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	/**
	 * Gestion de la connexion Ã la base de donnÃ©es PostgreSQL.
	 */
	// Les paramÃ¨tres de connexion Ã la base de donnÃ©es
	private static String username = "groupe6";
	private static String password = "groupe6"; // "m14dPOWA";
	private static String host = "VM-DISI-B2S-24";
	private static String numPort = "1433";
	private static String database = "groupe6";

	// L'instance de la connexion : elle reste active durant toute la durÃ©e de
	// vie de l'application.
	public static Connection connexion;

	// Etablissement de la connexion
	public DatabaseConnection() {
		initConnection();
	}

	/**
	 * CrÃ©e une connexion Ã la base de donnÃ©es (PostgreSQL). Les paramÃ¨tres
	 * de connexion Ã la base sont mÃ©morisÃ©s dans les attributs de la classe
	 * (username, password, etc.).)
	 */
	private static void initConnection() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://" + host + ";"
					+ "database=" + database + ";" + "user=" + username + ";"
					+ "password=" + password;
			connexion = DriverManager.getConnection(connectionUrl);
			;
			// Message OK
			// System.out.println("Connexion a " + base + " sur " + host +
			// " --> OK");
		} catch (SQLException | ClassNotFoundException e) {
			// Si erreur...
			System.err.println("Connexion a " + database + " sur " + host
					+ " impossibe !");
			System.err.println("Erreur : " + e.getMessage());
			// e.printStackTrace();
		}
	}

	/**
	 * Initialise une connexion ou renvoie la connexion existante.
	 * 
	 * @return l'instance de <code>Connection</code> permettant de rÃ©aliser des
	 *         opÃ©rations sur la base de donnÃ©es.
	 */
	public static Connection getConnection() {
		if (connexion == null) {
			initConnection();
		}
		return connexion;
	}

}
