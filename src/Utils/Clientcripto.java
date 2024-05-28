package Utils;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

import java.util.Base64;

public class Clientcripto {
    private SecretKey key;

    public void AuthenticationClient() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            this.key = generator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String setSimetricKey(String publicKey64) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey64);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        Cipher cipher =  Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);


        String message = "CHAVE_SIMETRICA " + this.key.getEncoded();
        byte[] encryptedAesKey = cipher.doFinal(message.getBytes());

        String encryptedKeyMessage = Base64.getEncoder().encodeToString(encryptedAesKey);
        
        return encryptedKeyMessage;
    }

    public String encryptedMessage(String Message) throws Exception {
        Cipher cif = Cipher.getInstance("AES");
        cif.init(Cipher.ENCRYPT_MODE, this.key);

        byte[] buffer = cif.doFinal(Message.getBytes());
        String messageToSend = Base64.getEncoder().encodeToString(buffer);

        return messageToSend;
    }

    public String decryptMessageFromClient(String message) throws Exception {
        byte[] messageBytes = Base64.getDecoder().decode(message);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, this.key);

        byte[] decryptedMessageBytes = cipher.doFinal(messageBytes);
        String decryptedMessage = new String(decryptedMessageBytes);

        return decryptedMessage;
    }
}