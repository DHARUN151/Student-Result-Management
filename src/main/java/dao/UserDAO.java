package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.User;
import util.DBConnection;
public class UserDAO{
    public User findUserByUsername(String username){
        User user=null;
        String sql="SELECT u.user_id,u.username,u.password_hash,u.account_status,u.student_id,u.faculty_id,u.first_login,u.two_factor_enabled,u.two_factor_secret,r.role_name FROM enterprise.users u JOIN enterprise.user_roles ur ON u.user_id=ur.user_id JOIN enterprise.roles r ON ur.role_id=r.role_id WHERE u.username=?";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){
            statement.setString(1,username);
            try(ResultSet result=statement.executeQuery()){
                if(result.next()){
                    user=new User();
                    user.setUserId(result.getInt("user_id"));
                    user.setUsername(result.getString("username"));
                    user.setPasswordHash(result.getString("password_hash"));
                    user.setAccountStatus(result.getString("account_status"));
                    int studentId=result.getInt("student_id");
                    if(!result.wasNull()){
                        user.setStudentId(studentId);
                    }
                    int facultyId=result.getInt("faculty_id");
                    if(!result.wasNull()){
                        user.setFacultyid(facultyId);
                    }
                    user.setFirstLogin(result.getBoolean("first_login"));
                    user.setTwoFactorEnabled(result.getBoolean("two_factor_enabled"));
                    user.setTwoFactorSecret(result.getString("two_factor_secret"));
                    user.setRole(result.getString("role_name"));
                }
            }
        }catch(Exception e){
            System.out.println("Error while finding user");
            System.out.println(e.getMessage());
        }
        return user;
    }
    public boolean isAccountLocked(String username){
        String sql="SELECT locked_until FROM enterprise.users WHERE username=?";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){
            statement.setString(1,username);
            try(ResultSet result=statement.executeQuery()){
                if(result.next()){
                    java.sql.Timestamp lockedUntil=result.getTimestamp("locked_until");
                    if(lockedUntil!=null){
                        if(lockedUntil.after(new java.sql.Timestamp(System.currentTimeMillis()))){
                            return true;
                        }
                        clearLoginAttempts(username);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public void recordFailedLogin(String username){
        String sql="UPDATE enterprise.users SET failed_login_attempts=failed_login_attempts+1,locked_until=CASE WHEN failed_login_attempts+1>=5 THEN CURRENT_TIMESTAMP+INTERVAL '15 minutes' ELSE locked_until END,updated_at=CURRENT_TIMESTAMP WHERE username=?";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){
            statement.setString(1,username);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void clearLoginAttempts(String username){
        String sql="UPDATE enterprise.users SET failed_login_attempts=0,locked_until=NULL,updated_at=CURRENT_TIMESTAMP WHERE username=?";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){
            statement.setString(1,username);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public int getFailedLoginAttempts(String username){
        String sql="SELECT failed_login_attempts FROM enterprise.users WHERE username=?";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){
            statement.setString(1,username);
            try(ResultSet result=statement.executeQuery()){
                if(result.next()){
                    return result.getInt("failed_login_attempts");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }
}