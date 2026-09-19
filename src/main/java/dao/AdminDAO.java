package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import util.DBConnection;
import java.sql.SQLException;
public class AdminDAO {
    public boolean authenticateAdmin(String username,String password) {
        String sql="SELECT u.password_hash FROM enterprise.users u JOIN enterprise.user_roles ur ON u.user_id=ur.user_id JOIN enterprise.roles r ON ur.role_id=r.role_id WHERE u.username=? AND u.account_status='ACTIVE' AND r.role_name IN('ADMIN','SUPER_ADMIN')";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,username);
            try(ResultSet rs=ps.executeQuery()) {
                if(rs.next()) {
                    return BCrypt.checkpw(password,rs.getString("password_hash"));
                }
            }
        } catch(Exception e) {
            System.out.println("Error while authenticating admin");
            System.out.println(e.getMessage());
        }
        return false;
    }

    public int getAdminUserId(String username) {
        String sql="SELECT u.user_id FROM enterprise.users u JOIN enterprise.user_roles ur ON u.user_id=ur.user_id JOIN enterprise.roles r ON ur.role_id=r.role_id WHERE u.username=? AND u.account_status='ACTIVE' AND r.role_name IN('ADMIN','SUPER_ADMIN')";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,username);
            try(ResultSet rs=ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch(Exception e) {
            System.out.println("Error while getting admin user id");
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public String getAdminRole(String username) {
        String sql="SELECT r.role_name FROM enterprise.users u JOIN enterprise.user_roles ur ON u.user_id=ur.user_id JOIN enterprise.roles r ON ur.role_id=r.role_id WHERE u.username=? AND u.account_status='ACTIVE' AND r.role_name IN('ADMIN','SUPER_ADMIN') ORDER BY CASE WHEN r.role_name='SUPER_ADMIN' THEN 1 ELSE 2 END LIMIT 1";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,username);
            try(ResultSet rs=ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getString("role_name");
                }
            }
        } catch(Exception e) {
            System.out.println("Error while getting admin role");
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean createAccount(String employeeCode,String name,String email,String phone,String departmentId,String designation,String password,String role,int adminUserId) {
        if(!"FACULTY".equals(role)&&!"HOD".equals(role)&&!"EXAM_CELL".equals(role)) {
            return false;
        }

        Connection con=null;

        try {
            con=DBConnection.getConnection();

            if(con==null) {
                return false;
            }

            con.setAutoCommit(false);

            String checkSql="SELECT user_id FROM enterprise.users WHERE username=?";

            try(PreparedStatement ps=con.prepareStatement(checkSql)) {
                ps.setString(1,employeeCode);

                try(ResultSet rs=ps.executeQuery()) {
                    if(rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            int facultyId=0;

            if("FACULTY".equals(role)||"HOD".equals(role)) {
                String facultySql="INSERT INTO enterprise.faculty(employee_code,name,email,phone,department_id,designation,status) VALUES(?,?,?,?,?,?,?)";

                try(PreparedStatement ps=con.prepareStatement(facultySql,java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1,employeeCode);
                    ps.setString(2,name);
                    ps.setString(3,email);
                    ps.setString(4,phone);
                    ps.setInt(5,Integer.parseInt(departmentId));
                    ps.setString(6,designation);
                    ps.setString(7,"ACTIVE");
                    ps.executeUpdate();

                    try(ResultSet rs=ps.getGeneratedKeys()) {
                        if(rs.next()) {
                            facultyId=rs.getInt(1);
                        }
                    }
                }

                if(facultyId==0) {
                    con.rollback();
                    return false;
                }
            }

            String hashedPassword=BCrypt.hashpw(password,BCrypt.gensalt());
            String userSql="INSERT INTO enterprise.users(username,password_hash,account_status,faculty_id,first_login) VALUES(?,?,?,?,?)";
            int userId=0;

            try(PreparedStatement ps=con.prepareStatement(userSql,java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1,employeeCode);
                ps.setString(2,hashedPassword);
                ps.setString(3,"ACTIVE");

                if(facultyId>0) {
                    ps.setInt(4,facultyId);
                } else {
                    ps.setNull(4,java.sql.Types.INTEGER);
                }

                ps.setBoolean(5,true);
                ps.executeUpdate();

                try(ResultSet rs=ps.getGeneratedKeys()) {
                    if(rs.next()) {
                        userId=rs.getInt(1);
                    }
                }
            }

            if(userId==0) {
                con.rollback();
                return false;
            }

            String roleSql="INSERT INTO enterprise.user_roles(user_id,role_id) SELECT ?,role_id FROM enterprise.roles WHERE role_name=?";

            try(PreparedStatement ps=con.prepareStatement(roleSql)) {
                ps.setInt(1,userId);
                ps.setString(2,role);

                if(ps.executeUpdate()!=1) {
                    con.rollback();
                    return false;
                }
            }

            String auditSql="INSERT INTO enterprise.audit_logs(user_id,action,entity_type,entity_id,new_value,remarks) VALUES(?,?,?,?,?,?)";

            try(PreparedStatement ps=con.prepareStatement(auditSql)) {
                ps.setInt(1,adminUserId);
                ps.setString(2,"CREATE_USER");
                ps.setString(3,role);
                ps.setString(4,String.valueOf(userId));
                ps.setString(5,"Username: "+employeeCode);
                ps.setString(6,"Account created by administrator");
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch(Exception e) {
            try {
                if(con!=null) {
                    con.rollback();
                }
            } catch(Exception rollbackException) {
                System.out.println("Rollback error: "+rollbackException.getMessage());
            }

            System.out.println("Error while creating account");
            System.out.println(e.getMessage());
            return false;

        } finally {
            try {
                if(con!=null) {
                    con.close();
                }
            } catch(Exception e) {
                System.out.println("Connection close error: "+e.getMessage());
            }
        }
    }

    public List<Map<String,Object>> getManagedAccounts(int page,int pageSize) {
        List<Map<String,Object>> list=new ArrayList<>();

        int offset=(page-1)*pageSize;

        String sql="SELECT u.user_id,u.username,u.account_status,u.created_at,"
                +"r.role_name,f.employee_code,f.name,f.email,f.designation,"
                +"f.status AS faculty_status "
                +"FROM enterprise.users u "
                +"JOIN enterprise.user_roles ur ON u.user_id=ur.user_id "
                +"JOIN enterprise.roles r ON ur.role_id=r.role_id "
                +"LEFT JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id "
                +"WHERE r.role_name IN('FACULTY','HOD','EXAM_CELL') "
                +"ORDER BY r.role_name,u.username "
                +"LIMIT ? OFFSET ?";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,pageSize);
            ps.setInt(2,offset);

            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) {
                    Map<String,Object> row=new HashMap<>();

                    row.put("userId",rs.getInt("user_id"));
                    row.put("username",rs.getString("username"));
                    row.put("accountStatus",rs.getString("account_status"));
                    row.put("role",rs.getString("role_name"));
                    row.put("employeeCode",rs.getString("employee_code"));
                    row.put("name",rs.getString("name"));
                    row.put("email",rs.getString("email"));
                    row.put("designation",rs.getString("designation"));
                    row.put("facultyStatus",rs.getString("faculty_status"));
                    row.put("createdAt",rs.getTimestamp("created_at"));

                    list.add(row);
                }
            }
        } catch(Exception e) {
            System.out.println("Error while getting managed accounts");
            System.out.println(e.getMessage());
        }

        return list;
    }

    public int getManagedAccountsCount() {
        String sql="SELECT COUNT(*) "
                +"FROM enterprise.users u "
                +"JOIN enterprise.user_roles ur ON u.user_id=ur.user_id "
                +"JOIN enterprise.roles r ON ur.role_id=r.role_id "
                +"WHERE r.role_name IN('FACULTY','HOD','EXAM_CELL')";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
            ResultSet rs=ps.executeQuery()) {

            if(rs.next()) {
                return rs.getInt(1);
            }
        } catch(Exception e) {
            System.out.println("Error while counting managed accounts");
            System.out.println(e.getMessage());
        }

        return 0;
    }

    public boolean deactivateAccount(int targetUserId,int adminUserId) {
        Connection con=null;

        try {
            con=DBConnection.getConnection();

            if(con==null) {
                return false;
            }

            con.setAutoCommit(false);

            String checkSql="SELECT u.faculty_id,r.role_name FROM enterprise.users u JOIN enterprise.user_roles ur ON u.user_id=ur.user_id JOIN enterprise.roles r ON ur.role_id=r.role_id WHERE u.user_id=? AND u.account_status='ACTIVE' AND r.role_name IN('FACULTY','HOD','EXAM_CELL')";

            int facultyId=0;
            String role=null;

            try(PreparedStatement ps=con.prepareStatement(checkSql)) {
                ps.setInt(1,targetUserId);

                try(ResultSet rs=ps.executeQuery()) {
                    if(!rs.next()) {
                        con.rollback();
                        return false;
                    }

                    facultyId=rs.getInt("faculty_id");

                    if(rs.wasNull()) {
                        facultyId=0;
                    }

                    role=rs.getString("role_name");
                }
            }

            String userSql="UPDATE enterprise.users SET account_status='INACTIVE',updated_at=CURRENT_TIMESTAMP WHERE user_id=? AND account_status='ACTIVE'";

            try(PreparedStatement ps=con.prepareStatement(userSql)) {
                ps.setInt(1,targetUserId);

                if(ps.executeUpdate()!=1) {
                    con.rollback();
                    return false;
                }
            }

            if(facultyId>0) {
                String facultySql="UPDATE enterprise.faculty SET status='INACTIVE',updated_at=CURRENT_TIMESTAMP WHERE faculty_id=?";

                try(PreparedStatement ps=con.prepareStatement(facultySql)) {
                    ps.setInt(1,facultyId);
                    ps.executeUpdate();
                }
            }

            String auditSql="INSERT INTO enterprise.audit_logs(user_id,action,entity_type,entity_id,old_value,new_value,remarks) VALUES(?,?,?,?,?,?,?)";

            try(PreparedStatement ps=con.prepareStatement(auditSql)) {
                ps.setInt(1,adminUserId);
                ps.setString(2,"DEACTIVATE_ACCOUNT");
                ps.setString(3,role);
                ps.setString(4,String.valueOf(targetUserId));
                ps.setString(5,"ACTIVE");
                ps.setString(6,"INACTIVE");
                ps.setString(7,"Account deactivated after owner 2FA verification");
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch(Exception e) {
            try {
                if(con!=null) {
                    con.rollback();
                }
            } catch(Exception rollbackException) {
                System.out.println("Rollback error: "+rollbackException.getMessage());
            }

            System.out.println("Error while deactivating account");
            System.out.println(e.getMessage());
            return false;

        } finally {
            try {
                if(con!=null) {
                    con.close();
                }
            } catch(Exception e) {
                System.out.println("Connection close error: "+e.getMessage());
            }
        }
    }
    public Map<String,Object> getManagedAccount(int userId) throws SQLException {
        String sql="SELECT u.user_id,u.username,u.account_status,u.created_at,"
                +"u.two_factor_enabled,u.two_factor_secret,"
                +"r.role_name,f.employee_code,f.name,f.email,f.designation,"
                +"f.status AS faculty_status "
                +"FROM enterprise.users u "
                +"JOIN enterprise.user_roles ur ON u.user_id=ur.user_id "
                +"JOIN enterprise.roles r ON ur.role_id=r.role_id "
                +"LEFT JOIN enterprise.faculty f ON u.faculty_id=f.faculty_id "
                +"WHERE u.user_id=? "
                +"AND r.role_name IN('FACULTY','HOD','EXAM_CELL') "
                +"LIMIT 1";

        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)) {

            ps.setInt(1,userId);

            try(ResultSet rs=ps.executeQuery()) {
                if(rs.next()) {
                    Map<String,Object> account=new HashMap<>();

                    account.put("userId",rs.getInt("user_id"));
                    account.put("username",rs.getString("username"));
                    account.put("accountStatus",rs.getString("account_status"));
                    account.put("createdAt",rs.getTimestamp("created_at"));
                    account.put("twoFactorEnabled",rs.getBoolean("two_factor_enabled"));
                    account.put("twoFactorSecret",rs.getString("two_factor_secret"));
                    account.put("role",rs.getString("role_name"));
                    account.put("employeeCode",rs.getString("employee_code"));
                    account.put("name",rs.getString("name"));
                    account.put("email",rs.getString("email"));
                    account.put("designation",rs.getString("designation"));
                    account.put("facultyStatus",rs.getString("faculty_status"));

                    return account;
                }
            }
        } catch(SQLException e) {
            System.out.println("Error while getting managed account");
            System.out.println(e.getMessage());
            throw e;
        }

        return null;
    }
    
}