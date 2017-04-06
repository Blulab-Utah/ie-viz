package edu.utah.blulab.commandline;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mysql.jdbc.Connection;

public class MySQL {

	private com.mysql.jdbc.Connection connection = null;
	private static MySQL mySQL = null;
	// private static String ConnectionString =
	// "jdbc:mysql://localhost/ieviz?user=root&password=lilith10&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";
	private static String LocalConnectionString = "jdbc:mysql://localhost/ieviz?user=root&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";
	private static String DockerConnectionString = "jdbc:mysql://0.0.0.0:6603/ieviz?user=root&password=mypassword&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";
	private static String ConnectionString = DockerConnectionString;
	
	public static void main(String[] args)  {
		try {
			Class driverClass = Class.forName("com.mysql.jdbc.Driver");
			driverClass.newInstance();

			Properties props = new Properties();
			String istr = String.valueOf(1024 * 1024 * 256);
			props.setProperty("maxAllowedPacket", istr);

//			String cstr = "jdbc:mysql://0.0.0.0:6603/ieviz?user=root&password=mypassword&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";
			
			String cstr = LocalConnectionString;
			cstr = DockerConnectionString;
			
			com.mysql.jdbc.Connection connection = (Connection) DriverManager.getConnection(cstr, props);
			connection.close();
			
			System.out.println("Connection succeeded...");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	public MySQL() {
		getConnection();
	}

	public static MySQL getMySQL() {
		if (mySQL == null) {
			mySQL = new MySQL();
		}
		return mySQL;
	}
	
	public void emptyTables() {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "delete from documents";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.execute();
			ps.close();
			sql = "delete from analyses";
			ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addDocumentAnalysis(NLPTool tool, String document, String analysis) {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "insert into analyses (tool, corpus, annotator, document, analysis) values (?, ?, ?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, tool.getToolName());
			ps.setString(2, tool.getCorpus());
			ps.setString(3, tool.getAnnotator());
			ps.setString(4, document);
			ps.setString(5, analysis);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addOntologyText(String oname, String otext) {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "insert into ontologies (name, text) values (?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, oname);
			ps.setString(2, otext);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addDocumentText(NLPTool tool, String document, String text) {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "insert into documents (corpus, document, text) values (?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, tool.getCorpus());
			ps.setString(2, document);
			ps.setString(3, text);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getOntologyText(String oname) {
		String otext = null;
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select text from ontologies where name = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, oname);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				otext = rs.getString(1);
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return otext;
	}

	public String getDocumentAnalysis(NLPTool tool, String document, String corpus) {
		String analysis = null;
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select analysis from analyses where corpus = ? and document = ? and annotator = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, tool.getCorpus());
			ps.setString(2, document);
			ps.setString(3, tool.getAnnotator());
			ps.execute();
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				analysis = rs.getString(1);
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return analysis;
	}

	public List<String> getDocumentAnalyses(String corpus, String annotator) {
		List<String> analyses = new ArrayList();
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select analysis from analyses where corpus = ? and annotator = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, corpus);
			ps.setString(2, annotator);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String analysis = rs.getString(1);
				analyses.add(analysis);
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return analyses;
	}

	public String getDocumentText(String docname, String corpname) {
		String text = null;
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select text from documents where document = ? and corpus = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, docname);
			ps.setString(2, corpname);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				text = rs.getString(1);
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return text;
	}

	public ArrayList<String> getDocumentNames(String corpname) {
		ArrayList<String> dnames = new ArrayList();
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select document from documents where corpus = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, corpname);
			ps.execute();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String dname = rs.getString(1);
				dnames.add(dname);
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dnames;
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

			String cstr = ConnectionString;
			
			System.out.println("MySQL.getConnection():  Cstr=" + cstr);
			
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
