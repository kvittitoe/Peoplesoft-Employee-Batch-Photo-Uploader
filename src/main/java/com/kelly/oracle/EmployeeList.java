package com.kelly.oracle;

import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmployeeList extends Connection {
	
	private static final Logger log = LogManager.getLogger(EmployeeList.class);
	
	private List<String> emplid;
	private Map<String, Date> creationDate;
	
	private final String SQL = "SELECT A.EMPLID "+
								"FROM SYSADM.PS_JOB A "+
								"WHERE A.EFFDT=(SELECT MAX(A1.EFFDT) "+
								" FROM SYSADM.PS_JOB A1 "+
								" WHERE A1.EMPLID=A.EMPLID "+
								" AND A1.EMPL_RCD=A.EMPL_RCD "+
								" AND A1.EFFDT<=SYSDATE) "+
								"AND A.EFFSEQ= (SELECT MAX(A1.EFFSEQ) "+
								" FROM SYSADM.PS_JOB A1 "+
								" WHERE A1.EMPLID=A.EMPLID "+
								" AND A1.EMPL_RCD=A.EMPL_RCD "+
								" AND A1.EFFDT=A.EFFDT) "+
								"AND A.COMPANY=\'ACT\' "+
								"AND A.JOB_INDICATOR=\'P\' "+
								"AND A.EMPL_STATUS = \'A\' "
								+ "ORDER BY A.EMPLID ASC";
	
	private final String FILES_SQL = "SELECT EMPLID, CREATION_DT FROM PS_WPS_EMPL_PHOTO";

	public EmployeeList(Map<String, String> database) {
		super(database);
		
		this.emplid=selectList(SQL, null, "EMPLID", true);
		log.info("Found "+ this.emplid.size()+" active employees.");
		
		this.creationDate = getCreationDateMap();
		log.info("Found "+this.creationDate.size()+" employees photos in the database.");
	}
	
	private Map<String, Date> getCreationDateMap(){
		Map<String, Date> temp = new HashMap<String, Date>();
		try {
			this.ps = this.connection.prepareStatement(FILES_SQL);
			
			this.rs = this.ps.executeQuery();
			
			while(this.rs.next()){
				String key = this.rs.getString("EMPLID");
				Date value = this.rs.getDate("CREATION_DT");
				temp.put(key, value);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			close();
		}
		
		return temp;
	}
	
	public List<String> getEmployees(){
		return this.emplid;
	}
	
	public Map<String, Date> getCreationDates(){
		return this.creationDate;
	}
}