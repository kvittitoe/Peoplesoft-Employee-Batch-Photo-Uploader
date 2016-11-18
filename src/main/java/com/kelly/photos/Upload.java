package com.kelly.photos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kelly.oracle.AddPhoto;
import com.kelly.oracle.EmployeeList;
import com.kelly.oracle.PhotoSizeDefinition;

public class Upload {
	
	private static final Logger log = LogManager.getLogger(Upload.class);
	private Map<String, String> database;
	private Map<String, String> paths;
	
	private List<String> employees;
	private Map<String, Date> creationDates;
	
	private final String CONFIG_PATH ="../conf/config.properties";
	
	private PhotoSizeDefinition photoSizes;
	
	public static void main(String[] args){
		new Upload();
	}
	
	public Upload(){
		getConfig();
		photoSizes = new PhotoSizeDefinition(database);
		
		EmployeeList employees = new EmployeeList(database);
		this.employees = employees.getEmployees();
		this.creationDates = employees.getCreationDates();
		
		findPhotos(this.employees);
	}
	
	private void findPhotos(List<String> employees){
		
		for(String emplid: employees){
			if(emplid.equals("10266")){
				System.out.println("");
			}
				
			processEmployee(emplid);
		}
	}
	
	private void processEmployee(String emplid){
		log.info("Processing emplid: "+emplid);
		Date creationDate = creationDates.get(emplid);
		
		boolean success = false;
		
		List<Photo> resizedPhotos = new ArrayList<Photo>();
		
		for(int i=0; i<this.photoSizes.size(); i++){
			String path = paths.get("input");
			String sizeName = photoSizes.getSizeName(i);
			int width = photoSizes.getWidth(i);
			int height = photoSizes.getHeight(i);
			String tempPath = paths.get("temp");
			
			log.info(path + ":" + sizeName+":" +width+":"+height+":"+tempPath);
			
			Photo photo = new Photo(path, emplid, sizeName, width, height, tempPath, creationDate);
			if(photo.exists){
				resizedPhotos.add(photo);
				success = true;
			}else{
				success = false;
				break;
			}
		}
		if(success){
			new AddPhoto(database, resizedPhotos, emplid);
		}
		
	}
	
	/**
	 * Get configuration detail for the program
	 */
	private void getConfig(){
		log.info("Reading from the configuration file");
		
		database = new HashMap<String,String>();
		paths = new HashMap<String,String>(); 
		
		Properties prop = new Properties();
		InputStream input = null;

		try {
			File file = new File(CONFIG_PATH);
			log.info("Looking for config file @ " + file.getAbsolutePath());
			input = new FileInputStream(CONFIG_PATH);
			prop.load(input);

			database.put("url", prop.getProperty("database.url"));
			database.put("port", prop.getProperty("database.port"));
			database.put("name", prop.getProperty("database.name"));
			database.put("username", prop.getProperty("database.username"));
			database.put("password", prop.getProperty("database.password"));
			
			paths.put("input", prop.getProperty("directory.input"));
			paths.put("temp", prop.getProperty("directory.temp"));
			
			log.info("Read properties file successfully.");

		} catch(java.io.FileNotFoundException e){
			log.fatal("File not found: "+CONFIG_PATH+".", e);
		} catch (java.io.IOException e) {
			log.fatal("Unable to read the file: "+CONFIG_PATH+".", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (java.io.IOException e) {
					log.warn("Unable to close connection.properties stream.", e);
				}
			}
		}
	}
}
