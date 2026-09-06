package model;
import java.sql.Date;

public class Student {
	
	private int studId;
	private String regNum;
	private String name;
	private Date dob;
	private String gender;
	private String phone;
	private String email;
	
	public Student() {
		
	}
	
	public Student(int studId, String regNum, String name, Date dob,
            String gender, String phone, String email) {

	 this.studId = studId;
	 this.regNum = regNum;
	 this.name = name;
	 this.dob = dob;
	 this.gender = gender;
	 this.phone = phone;
	 this.email = email;
	}
	
	public int getStudId() {
	 return studId;
	}
	
	public void setStudId(int studId) {
	 this.studId = studId;
	}
	
	public String getRegNum() {
	 return regNum;
	}
	
	public void setRegNum(String regNum) {
	 this.regNum = regNum;
	}
	
	public String getName() {
	 return name;
	}
	
	public void setName(String name) {
	 this.name = name;
	}
	
	public Date getDob() {
	 return dob;
	}
	
	public void setDob(Date dob) {
	 this.dob = dob;
	}
	
	public String getGender() {
	 return gender;
	}
	
	public void setGender(String gender) {
	 this.gender = gender;
	}
	
	public String getPhone() {
	 return phone;
	}
	
	public void setPhone(String phone) {
	 this.phone = phone;
	}
	
	public String getEmail() {
	 return email;
	}
	
	public void setEmail(String email) {
	 this.email = email;
	}
}
