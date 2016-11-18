package com.kelly.oracle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhotoSizeDefinition extends Connection{
	
	private static final Logger log = LogManager.getLogger(PhotoSizeDefinition.class);
	
	private boolean success;
		
	private List<String> sizeName = new ArrayList<String>();
	private List<Integer> width = new ArrayList<Integer>();
	private List<Integer> height = new ArrayList<Integer>();

	public PhotoSizeDefinition(Map<String, String> database) {
		super(database);
		this.success = query();
		close();
	}

	/**
	 * Get peoplesoft photo size definitions
	 * @return
	 */
	private boolean query(){
		
		String sql = "SELECT PHOTO_SIZENAME, PHOTO_WIDTH, PHOTO_HEIGHT from PS_PHOTO_SIZEDEFN WHERE SYSTEM_DATA_FLG='Y'";
		
		boolean success = false;
		
		try {
			this.ps = this.connection.prepareStatement(sql);
			
			this.rs = this.ps.executeQuery();
			while(rs.next()){
				sizeName.add(rs.getString("PHOTO_SIZENAME"));
				width.add(rs.getInt("PHOTO_WIDTH"));
				height.add(rs.getInt("PHOTO_HEIGHT"));
			}
			success = true;
			log.info("Successfully retiieved size definitions from database.");
		} catch (SQLException e) {
			log.fatal("Failed to execute the query.", e);
		}
		
		return success;
	}
	
	public String getSizeName(int index){
		return this.sizeName.get(index);
	}
	
	public int getWidth(int index){
		return this.width.get(index);
	}
	
	public int getHeight(int index){
		return this.height.get(index);
	}
	
	public int size(){
		return this.sizeName.size();
	}
	
	public boolean isSuccessful(){
		return success;
	}
}
