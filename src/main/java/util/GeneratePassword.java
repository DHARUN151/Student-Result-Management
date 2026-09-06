package util;
import org.mindrot.jbcrypt.BCrypt;
public class GeneratePassword{
    public static void main(String[] args){
        String password="examcell123";
        String hash=BCrypt.hashpw(password,BCrypt.gensalt(10));
        System.out.println(hash);
    }
}