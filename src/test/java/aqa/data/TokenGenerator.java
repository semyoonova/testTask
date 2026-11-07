package aqa.data;

import java.util.Random;

import aqa.config.Config;

public final class TokenGenerator {

    public static final String HEX = "0123456789ABCDEF"; //TODO: в конфиги 
    public static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //TODO: в конфиги 
    private static final Random Rand = new Random();

    public static String generate(String alphabet, int length) {
        StringBuilder token = new StringBuilder(length);  
  
        for (int i = 0; i < length; i++) {  
            token.append(alphabet.charAt(Rand.nextInt(alphabet.length())));  
        }
        
        return token.toString();
    }

    public static String validToken() {
        return generate(HEX, Integer.parseInt(Config.get("tokenLength")));
    }
}
