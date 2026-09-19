package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import util.DBConnection;

public class PermissionDAO {
    public boolean hasPermission(int userId,String permissionName) {
        String sql="SELECT COUNT(*) FROM enterprise.user_roles ur JOIN enterprise.role_permissions rp ON ur.role_id=rp.role_id JOIN enterprise.permissions p ON rp.permission_id=p.permission_id WHERE ur.user_id=? AND p.permission_name=?";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)) {
            statement.setInt(1,userId);
            statement.setString(2,permissionName);
            try(ResultSet result=statement.executeQuery()) {
                if(result.next()) {
                    return result.getInt(1)>0;
                }
            }
        } catch(Exception e) {
            System.out.println("Error while checking permission");
            System.out.println(e.getMessage());
        }
        return false;
    }
}