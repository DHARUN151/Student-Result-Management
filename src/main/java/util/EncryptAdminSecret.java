package util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class EncryptAdminSecret{
    public static void main(String[] args){
        String selectSql="SELECT two_factor_secret FROM enterprise.users WHERE username='admin'";
        String updateSql="UPDATE enterprise.users SET two_factor_secret=? WHERE username='admin'";
        try(Connection connection=DBConnection.getConnection();PreparedStatement select=connection.prepareStatement(selectSql);ResultSet result=select.executeQuery()){
            if(result.next()){
                String secret=result.getString("two_factor_secret");
                if(secret==null||secret.isEmpty()){
                    System.out.println("Admin 2FA secret is empty.");
                    return;
                }
                String encryptedSecret=TwoFactorUtil.encryptSecret(secret);
                try(PreparedStatement update=connection.prepareStatement(updateSql)){
                    update.setString(1,encryptedSecret);
                    int updated=update.executeUpdate();
                    if(updated==1){
                        System.out.println("Admin 2FA secret encrypted successfully.");
                    }else{
                        System.out.println("Admin 2FA secret was not updated.");
                    }
                }
            }else{
                System.out.println("Admin account not found.");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}