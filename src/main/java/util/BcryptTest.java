package util;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptTest {

    public static void main(String[] args) {

        String password = "teacher123";

        String hashedPassword =
                BCrypt.hashpw(password, BCrypt.gensalt(10));

        System.out.println("Original Password:");
        System.out.println(password);

        System.out.println("Hashed Password:");
        System.out.println(hashedPassword);

        if (BCrypt.checkpw(password, hashedPassword)) {
            System.out.println("Password is correct");
        } else {
            System.out.println("Password is incorrect");
        }
    }
}