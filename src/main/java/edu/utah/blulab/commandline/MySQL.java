package edu.utah.blulab.commandline;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Connection;

public class MySQL {

	private com.mysql.jdbc.Connection connection = null;
	private static MySQL mySQL = null;
	private static String ConnectionString = "jdbc:mysql://localhost/ievizdb?user=root&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";

	public MySQL() {
		getConnection();
	}

	public static MySQL getMySQL() {
		if (mySQL == null) {
			mySQL = new MySQL();
		}
		return mySQL;
	}

	public void addDocumentAnalysis(String tool, String docname, String corpname, String analysis) {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "insert tool = ?, document = ?, corpus = ?, analysis = ? into ANALYSIS";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, tool);
			ps.setString(2, docname);
			ps.setString(3, corpname);
			ps.setString(4, analysis);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addDocumentText(String tool, String docname, String corpname, String text) {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "insert tool = ?, document = ?, corpus = ?, text = ? into DOCUMENT";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, tool);
			ps.setString(2, docname);
			ps.setString(3, corpname);
			ps.setString(4, text);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getDocumentAnalysis(String toolname, String docname, String corpname) {
		String analysis = null;
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select analysis from ANALYSIS where tool = ? and document = ? and corpus = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, toolname);
			ps.setString(2, docname);
			ps.setString(3, corpname);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				analysis = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return analysis;
	}

	public String getDocumentText(String toolname, String docname, String corpname) {
		String text = null;
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select text from DOCUMENT where tool = ? and document = ? and corpus = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, toolname);
			ps.setString(2, docname);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				text = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return text;
	}

	public Connection getConnection() {
		try {
			if (this.connection != null) {
				return this.connection;
			}
			Class driverClass = Class.forName("com.mysql.jdbc.Driver");
			driverClass.newInstance();

			Properties props = new Properties();
			String istr = String.valueOf(1024 * 1024 * 256);
			props.setProperty("maxAllowedPacket", istr);

			this.connection = (Connection) DriverManager.getConnection(ConnectionString, props);

			System.out.println("Connection succeeded...");
			return connection;
		} catch (Exception ex) {
			System.out.println("SQLException: " + ex.getMessage());
		}
		return null;
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
		}
		connection = null;
	}

}
