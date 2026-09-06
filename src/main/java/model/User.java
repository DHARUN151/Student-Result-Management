package model;
public class User{
    private String username;
    private String passwordHash;
    private String role;
    private String accountStatus;
    private Integer studentId;
    private Integer facultyid;
    private int userId;
    private boolean firstLogin;
    private boolean twoFactorEnabled;
    private String twoFactorSecret;
    public User(){
    }
    public int getUserId(){
        return userId;
    }
    public void setUserId(int userId){
        this.userId=userId;
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username=username;
    }
    public String getPasswordHash(){
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash){
        this.passwordHash=passwordHash;
    }
    public String getRole(){
        return role;
    }
    public void setRole(String role){
        this.role=role;
    }
    public String getAccountStatus(){
        return accountStatus;
    }
    public void setAccountStatus(String accountStatus){
        this.accountStatus=accountStatus;
    }
    public Integer getStudentId(){
        return studentId;
    }
    public void setStudentId(Integer studentId){
        this.studentId=studentId;
    }
    public Integer getFacultyid(){
        return facultyid;
    }
    public void setFacultyid(Integer facultyid){
        this.facultyid=facultyid;
    }
    public boolean isFirstLogin(){
        return firstLogin;
    }
    public void setFirstLogin(boolean firstLogin){
        this.firstLogin=firstLogin;
    }
    public boolean isTwoFactorEnabled(){
        return twoFactorEnabled;
    }
    public void setTwoFactorEnabled(boolean twoFactorEnabled){
        this.twoFactorEnabled=twoFactorEnabled;
    }
    public String getTwoFactorSecret(){
        return twoFactorSecret;
    }
    public void setTwoFactorSecret(String twoFactorSecret){
        this.twoFactorSecret=twoFactorSecret;
    }
}