/**
 * 
 */
package rexos.mas.data.sqldatadase;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import rexos.libraries.log.Logger;
import rexos.mas.data.LogMessage;

/**
 * @author Theodoor
 *
 */
public class sqliteDatabase {

	static final String JDBC_DRIVER = "org.sqlite.JDBC";
	static final String DB_URL = "jdbc:sqlite:";
	String DB_FILE = "default.sqlite3";
	

	// Database credentials
	static final String USER = "username";
	static final String PASS = "password";
	
	Connection conn = null;
	Statement stmt = null;		
	
	public sqliteDatabase(String filename){
			if(filename != null){DB_FILE = filename;}
			try {
				Driver d = (Driver) Class.forName(JDBC_DRIVER).newInstance();
				DriverManager.registerDriver(d);
			} catch (Exception e) {
				System.out.println("Error loading database driver: "
						+ e.toString());
				return;
			}
			
			System.out.println("Connecting to database...");
			try {
				conn = DriverManager.getConnection(DB_URL + DB_FILE, USER, PASS);
				PreparedStatement create = conn.prepareStatement("CREATE TABLE IF NOT EXISTS log (id INT, time VARCHAR(30), state VARCHAR(30), message VARCHAR(250))");
				create.execute();
			} catch (SQLException e) {
				Logger.log(e);
			}
	}
	
	/**
	 * @param msg
	 */
	public void insert(List<LogMessage> msgs) {
		try {
			PreparedStatement insert = conn.prepareStatement("INSERT INTO LOG (id, time, state, message) VALUES (?, ?, ?, ?)");
			
			for(LogMessage msg: msgs){
				insert.setString(1, msg.getId());
				insert.setString(2, msg.getTime());
				insert.setString(3, msg.getState());
				insert.setString(4, msg.getMessage());
				insert.execute();
			}
		} catch (SQLException e) {
			Logger.log(e);
		}		
	}

}