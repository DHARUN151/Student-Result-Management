package util;
import java.security.SecureRandom;
import java.util.Base64;
public class GenerateEncryptionKey{
    public static void main(String[] args){
        byte[] key=new byte[32];
        new SecureRandom().nextBytes(key);
        System.out.println(Base64.getEncoder().encodeToString(key));
    }
}