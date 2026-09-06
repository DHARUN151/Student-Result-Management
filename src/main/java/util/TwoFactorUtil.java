package util;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
public class TwoFactorUtil{
    private static final String BASE32_CHARS="ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM=new SecureRandom();
    private static final int AES_KEY_SIZE=32;
    private static final int GCM_TAG_LENGTH=128;
    public static String generateSecret(){
        byte[] bytes=new byte[20];
        RANDOM.nextBytes(bytes);
        return base32Encode(bytes);
    }
    public static boolean verifyCode(String secret,String code){
        if(secret==null||code==null||!code.matches("\\d{6}")){
            return false;
        }
        long currentTime=System.currentTimeMillis()/1000L/30L;
        for(long offset=-1;offset<=1;offset++){
            if(generateCode(secret,currentTime+offset).equals(code)){
                return true;
            }
        }
        return false;
    }
    public static String generateCode(String secret,long counter){
        try{
            byte[] key=base32Decode(secret);
            byte[] data=ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac=Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key,"HmacSHA1"));
            byte[] hash=mac.doFinal(data);
            int offset=hash[hash.length-1]&0x0f;
            int binary=((hash[offset]&0x7f)<<24)|((hash[offset+1]&0xff)<<16)|((hash[offset+2]&0xff)<<8)|(hash[offset+3]&0xff);
            int otp=binary%1000000;
            return String.format("%06d",otp);
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    public static String encryptSecret(String secret)throws Exception{
        byte[] key=getEncryptionKey();
        byte[] iv=new byte[12];
        RANDOM.nextBytes(iv);
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec=new SecretKeySpec(key,"AES");
        GCMParameterSpec parameterSpec=new GCMParameterSpec(GCM_TAG_LENGTH,iv);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,parameterSpec);
        byte[] encrypted=cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer=ByteBuffer.allocate(iv.length+encrypted.length);
        buffer.put(iv);
        buffer.put(encrypted);
        return Base64.getEncoder().encodeToString(buffer.array());
    }
    public static String decryptSecret(String encryptedSecret)throws Exception{
        byte[] key=getEncryptionKey();
        byte[] data=Base64.getDecoder().decode(encryptedSecret);
        byte[] iv=new byte[12];
        byte[] encrypted=new byte[data.length-12];
        System.arraycopy(data,0,iv,0,12);
        System.arraycopy(data,12,encrypted,0,encrypted.length);
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec=new SecretKeySpec(key,"AES");
        GCMParameterSpec parameterSpec=new GCMParameterSpec(GCM_TAG_LENGTH,iv);
        cipher.init(Cipher.DECRYPT_MODE,keySpec,parameterSpec);
        byte[] decrypted=cipher.doFinal(encrypted);
        return new String(decrypted,StandardCharsets.UTF_8);
    }
    private static byte[] getEncryptionKey(){
        String value=System.getenv("TWO_FACTOR_ENCRYPTION_KEY");
        if(value==null||value.trim().isEmpty()){
            throw new IllegalStateException("TWO_FACTOR_ENCRYPTION_KEY environment variable is not configured.");
        }
        byte[] key=Base64.getDecoder().decode(value);
        if(key.length!=AES_KEY_SIZE){
            throw new IllegalStateException("TWO_FACTOR_ENCRYPTION_KEY must contain exactly 32 bytes.");
        }
        return key;
    }
    public static String getOtpAuthUri(String username,String secret){
        String issuer="Student Result Management System";
        return "otpauth://totp/"+urlEncode(issuer)+":"+urlEncode(username)+"?secret="+secret+"&issuer="+urlEncode(issuer)+"&algorithm=SHA1&digits=6&period=30";
    }
    private static String urlEncode(String value){
        return value.replace(" ","%20");
    }
    private static String base32Encode(byte[] data){
        StringBuilder result=new StringBuilder();
        int buffer=0;
        int bitsLeft=0;
        for(byte b:data){
            buffer=(buffer<<8)|(b&0xff);
            bitsLeft+=8;
            while(bitsLeft>=5){
                int index=(buffer>>(bitsLeft-5))&31;
                result.append(BASE32_CHARS.charAt(index));
                bitsLeft-=5;
            }
        }
        if(bitsLeft>0){
            int index=(buffer<<(5-bitsLeft))&31;
            result.append(BASE32_CHARS.charAt(index));
        }
        return result.toString();
    }
    private static byte[] base32Decode(String value){
        value=value.replace("=","").replace(" ","").toUpperCase();
        ByteBuffer buffer=ByteBuffer.allocate(value.length()*5/8+1);
        int current=0;
        int bitsLeft=0;
        for(char c:value.toCharArray()){
            int index=BASE32_CHARS.indexOf(c);
            if(index<0){
                continue;
            }
            current=(current<<5)|index;
            bitsLeft+=5;
            if(bitsLeft>=8){
                buffer.put((byte)((current>>(bitsLeft-8))&0xff));
                bitsLeft-=8;
            }
        }
        byte[] result=new byte[buffer.position()];
        buffer.flip();
        buffer.get(result);
        return result;
    }
}