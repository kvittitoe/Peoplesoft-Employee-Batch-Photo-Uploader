package com.kelly.photos;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Photo {
	
	private static final Logger log = LogManager.getLogger(Photo.class);

	private File resizedPath;
	private java.sql.Date lastModified;
	protected boolean exists, updated;
	private int type;
	
	private String emplid;
	private String sizeName;
	private int width, height;
	
	public Photo(String path, String emplid, String sizeName, int width, int height, String tempPath, Date oldDate){
		this.emplid=emplid;
		this.sizeName=sizeName;
		this.width=width;
		this.height=height;
		
		process(emplid, path, width, height, tempPath, oldDate);
	}
	
	/**
	 * Process and resize the picture.
	 * @param emplid
	 * @param path
	 * @param width
	 * @param height
	 */
	private void process(String emplid, String path, int width, int height, String tempPath, Date oldDate){
		
		File photoPath = getPhotoPath(emplid, path);
		log.info("Original photo location is: "+photoPath.getPath());
		
		if (photoPath.exists()){
			this.exists=true;
			this.lastModified = getDate(photoPath.lastModified());
			
			updated = oldDate != null;
			
			if(isNewPicture(oldDate, this.lastModified)){
				log.info("Found new picture for employee: "+emplid);
				
				resizeImage(getImage(photoPath), this.type, width, height, tempPath);
			}else{
				log.info("Found existing picture for "+emplid+" skipping resizing step.");
				this.exists=false;
			}
			
		}else{
			log.warn("Cannot find photo for employee: "+emplid);
			this.exists=false;
		}
	}
	
	private Date getDate(long time){
		Timestamp ts = new Timestamp(time);
		return new Date(ts.getTime());
		
	}
	
	private boolean isNewPicture(Date oldDate, Date lastModified){
		if(oldDate != null){
			int diff = oldDate.compareTo(lastModified);
			
			if(diff>0){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
	
	private void resizeImage(BufferedImage image, int type, int width, int height, String tempPath){
		String name = rename(emplid, width, height);
		BufferedImage resizeImageJpg = resizeImage(image, this.type, width, height);
		this.resizedPath = new File(tempPath+name);
		log.info("Writing resized photo: "+resizedPath.getPath());
		try {
			ImageIO.write(resizeImageJpg, "jpg",resizedPath);
			log.info("Successfully resized the employee photo!");
		} catch (IOException e) {
			log.fatal("Failed to resize the photo.", e);
		}
	}
	
	private static BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height){
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}
	
	private BufferedImage getImage(File path){
		log.info("Reading image from: "+ path.getPath());
		BufferedImage image = null;
		try {
			image = ImageIO.read(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
		return image;
	}
	
	public boolean destory(){
		boolean success = false;
		if(resizedPath.exists()){
			success = resizedPath.delete();
		}
		return success;
	}
	
	private File getPhotoPath(String emplid, String path){
		return new File(path + emplid+ ".jpg");
	}
	
	private String rename(String emplid, int width, int height){
		return emplid + "_" +width+"_" + height+".jpg";
	}
	
	public String getEmplid() {
		return emplid;
	}

	public String getSizeName() {
		return sizeName;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public FileInputStream getResized() {
		FileInputStream fis = null;
		try {
			fis= new FileInputStream(this.resizedPath);
		} catch (FileNotFoundException e) {
			log.fatal("Cannot find the resized photo. "+resizedPath.getPath(), e);
		}
		return fis;
	}
	
	public String getPhotoName(){
		return resizedPath.getName();
	}
	
	public Date getCreationDate(){
		return lastModified;
	}
	
	public boolean isUpdated(){
		return updated;
	}
}
