package com.kelly.oracle;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kelly.photos.Photo;

public class AddPhoto extends Connection {
	
	private static final Logger log = LogManager.getLogger(AddPhoto.class);
	
	private Date creationDate;
	private boolean updated;

	public AddPhoto(Map<String, String> database, List<Photo> photos, String emplid) {
		super(database);
		
		boolean success = removeOldPhotos(emplid);
		if(success){
			log.info("Removed old employee photos from the database.");
		}else{
			log.warn("Failed to remove old employee photos from the database.");
		}
		
		success = insertPictures(photos);
		
		if(success){
			if(updated){
				update(emplid, creationDate);
			}else{
				insert(emplid, creationDate);
			}
		}else{
			log.info("Skipping photo import; current photo for employee already exists.");
		}
		
		cleanup(photos);
	}
	
	private boolean insertPictures(List<Photo> photos){
		
		if(photos.size() == 0){
			return false;
		}
		
		boolean success = false;
		
		StringBuilder sql = new StringBuilder("INSERT ALL ");
		for(Photo photo: photos){
			this.creationDate = photo.getCreationDate();
			this.updated = photo.isUpdated();
			sql.append("INTO SYSADM.PS_EMPL_PHOTO (EMPLID, PHOTO_SIZENAME, PHOTO_IMGNAME, PSIMAGEVER, EMPLOYEE_PHOTO) VALUES(?,?,?,?,?) ");
		}
		sql.append("SELECT * FROM dual");
		
		List<FileInputStream> openStreams = new ArrayList<FileInputStream>();
		
		try {
			this.ps = this.connection.prepareStatement(sql.toString());
			
			log.info(sql.toString());
			
			int index=1;
			
			for(Photo photo: photos){
				ps.setString(index++, photo.getEmplid());
				ps.setString(index++, photo.getSizeName());
				ps.setString(index++, photo.getPhotoName());
				ps.setLong(index++, Long.parseLong(photo.getEmplid()+index));
				FileInputStream image = photo.getResized();
				openStreams.add(image);
				ps.setBinaryStream(index++, image, image.available());
			}
			
			this.ps.executeUpdate();
			
			success = true;
			log.info("Successfully inserted employe photos into database.");
			
		} catch (SQLException e) {
			log.fatal("Failed to insert image into the database.", e);
		} catch (IOException e) {
			log.fatal("Image is not available.", e);
		}finally{
			for(FileInputStream fis: openStreams){
				try {
					fis.close();
				} catch (IOException e) {
					log.warn("Failed to close image input stream.", e);
				}
			}
		}
		
		return success;
	}
	
	private boolean update(String emplid, Date creationDate){
		log.info("Updating the photo tracking table.");
		String sql = "UPDATE PS_WPS_EMPL_PHOTO SET CREATION_DT=? WHERE EMPLID=?";
		List<String> parameters = new ArrayList<String>();
		parameters.add(dateToStr(creationDate));
		parameters.add(emplid);
		boolean success = query(sql, parameters, false);
		return success;
	}
	
	private boolean insert(String emplid, Date creationDate){
		log.info("Inserting into the photo tracking table.");
		String sql = "INSERT INTO PS_WPS_EMPL_PHOTO (EMPLID, CREATION_DT) VALUES(?,?)";
		List<String> parameters = new ArrayList<String>();
		parameters.add(emplid);
		parameters.add(dateToStr(creationDate));
		boolean success = query(sql, parameters, false);
		return success;
	}
	
	private String dateToStr(Date date){
		DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		String str = df.format(date);
		return str;
				
	}

	private boolean removeOldPhotos(String emplid){
		String sql = "DELETE FROM SYSADM.PS_EMPL_PHOTO WHERE EMPLID=?";
		
		List<String> parameters = new ArrayList<String>();
		parameters.add(emplid);
		return query(sql, parameters, true);
	}
	
	private void cleanup(List<Photo> photos){
		for(Photo photo: photos){
			photo.destory();
		}
	}
}
