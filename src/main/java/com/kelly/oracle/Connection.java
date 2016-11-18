package com.kelly.oracle;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Connection {
	private static final Logger log = LogManager.getLogger(Connection.class);
	
	protected java.sql.Connection connection;
	protected PreparedStatement ps;
	protected ResultSet rs;
	
	private Map<String, String> database;
	
	public Connection(Map<String,String> database){
		this.database = database;
		this.connection = connect(database);
	}
	
	/**
	 * Establish the database connection
	 * @return
	 */
	private java.sql.Connection connect(Map<String, String> database){
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			log.fatal("Cannot find the oracle driver.", e);
		}

		StringBuilder connString = new StringBuilder();
		connString.append("jdbc:oracle:thin:@");
		connString.append(database.get("url")).append(":");
		connString.append(database.get("port")).append(":");
		connString.append(database.get("name"));
		
		try {
			java.sql.Connection conn =  DriverManager.getConnection(
					connString.toString(), 
					database.get("username"),
					database.get("password"));
			
			log.info("Connected to the database successfully!");
			return conn;

		} catch (SQLException e) {
			log.fatal("Connection Failed!", e);
		}
		return null;
	}
	
	/**
	 * default query which allows string parameters only.
	 * @param sql
	 * @param parameters
	 * @param keepAlive
	 * @return
	 * @throws SQLException
	 */
	protected boolean query(String sql, List<String> parameters, boolean keepAlive){
		
		log.info("Executing query: "+sql);
		
		boolean success = false;
		
		try {
			this.ps = this.connection.prepareStatement(sql);
			for(int i=0; i<parameters.size(); i++){
				this.ps.setString(i+1, parameters.get(i));
			}
			this.ps.executeUpdate();
			success = true;
			
		} catch (SQLException e) {
			log.fatal("Failed to execute the query.", e);
		}
		
		if(!keepAlive){
			close();
		}
		
		return success;
	}
	
	protected String selectValue(String sql, List<String> parameters, String column, boolean keepAlive){
		
		log.info("Executing select query: "+sql);
		
		String temp = null;
		
		try {
			this.ps = this.connection.prepareStatement(sql);
			for(int i=0; i<=parameters.size(); i++){
				this.ps.setString(i+1, parameters.get(i));
			}
			this.rs = this.ps.executeQuery();
			if(rs.next()){
				temp = rs.getString(column);
				log.info("Successfully selected: '"+temp+"' from column: '"+column+"'." );
			}
		} catch (SQLException e) {
			log.fatal("Failed to execute the select statement.", e);
		}
		
		return temp;
	}
	
	protected List<String> selectList(String sql, List<String> parameters, String column, boolean keepAlive){
		
		log.info("Executing select query: "+sql);
		
		List<String> temp = new ArrayList<String>();
		
		try {
			this.ps = this.connection.prepareStatement(sql);
			if(parameters != null){
				for(int i=0; i<=parameters.size(); i++){
					this.ps.setString(i+1, parameters.get(i));
				}
			}
			this.rs = this.ps.executeQuery();
			while(rs.next()){
				temp.add(rs.getString(column));
			}
			log.info("Successfully selected values from column: '"+column+"'." );
		} catch (SQLException e) {
			log.fatal("Failed to execute the select statement.", e);
		}
		
		if(!keepAlive){
			close();
		}
		
		return temp;
	}
	
	private void closeResultSet(){
		if(this.rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				log.warn("Failed to close the result set");
			}
		}
	}
	
	private void closePreparedStatement(){
		if(this.ps != null){
			try {
				ps.close();
			} catch (SQLException e) {
				log.warn("Failed to close the prepared statement.");
			}
		}
	}
	
	private void closeConnection(){
		if(this.connection != null){
			try {
				connection.close();
			} catch (SQLException e) {
				log.warn("Failed to close the connection.");
			}
		}
	}
	
	/**
	 * Close all open stream objects.
	 */
	public void close(){
		log.info("Closing the connection and related streams.");
		closeResultSet();
		closePreparedStatement();
		closeConnection();
	}
	
	/**
	 * Close the connection, and establish a new connection
	 */
	public void refresh(){
		close();
		this.connection = connect(database);
	}
}

