//package util;
//import java.sql.Connection;
//import java.sql.DriverManager;
//public class DBConnection {
//private static final String URL="jdbc:postgresql://localhost:5432/Student_Result_DB";
//private static final String user="postgres";
//private static final String password="3014";
//public static Connection getConnection() {
//try {
//Class.forName("org.postgresql.Driver");
//return DriverManager.getConnection(URL,user,password);
//}catch(Exception e){
//e.printStackTrace();
//return null;
//}
//}
//}
//package util;
//import java.sql.Connection;
//import java.sql.DriverManager;
//public class DBConnection{
//    public static Connection getConnection(){
//        try{
//            String url=System.getenv("DB_URL");
//            String user=System.getenv("DB_USERNAME");
//            String password=System.getenv("DB_PASSWORD");
//            if(url==null||url.isEmpty()){
//                url="jdbc:postgresql://localhost:5432/Student_Result_DB";
//            }
//            if(user==null||user.isEmpty()){
//                user="postgres";
//            }
//            if(password==null||password.isEmpty()){
//                password="3014";
//            }
//            Class.forName("org.postgresql.Driver");
//            return DriverManager.getConnection(url,user,password);
//        }catch(Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }
//}


package util;
import java.sql.Connection;
import java.sql.DriverManager;
public class DBConnection{
    private static final String LOCAL_URL="jdbc:postgresql://localhost:5432/Student_Result_DB";
    private static final String LOCAL_USER="postgres";
    private static final String LOCAL_PASSWORD="3014";
    public static Connection getConnection(){
        try{
            Class.forName("org.postgresql.Driver");
            String url=System.getenv("DB_URL");
            String user=System.getenv("DB_USERNAME");
            String password=System.getenv("DB_PASSWORD");
            if(url==null||url.isBlank()){
                url=LOCAL_URL;
                user=LOCAL_USER;
                password=LOCAL_PASSWORD;
            }
            return DriverManager.getConnection(url,user,password);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}