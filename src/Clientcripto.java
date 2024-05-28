import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;

public class Clientcripto {
    private String simetricKeyCripto;
    private static SecretKey key;
    private ArrayList<SecretKey> keys;
    private static byte[] msgEncriptada;


    public void AuthenticationClient() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            this.key = generator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setSimetricKey(String publicKey64) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey64);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Cipher cipher =  Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedAesKey = cipher.doFinal(this.key.getEncoded());

            this.simetricKeyCripto = Base64.getEncoder().encodeToString(encryptedAesKey);
            byte[] chave = this.key.getEncoded();
            String encryptedKeyMessageBase64 = Base64.getEncoder().encodeToString(this.simetricKeyCripto.getBytes());
            String encryptedKeyMessage = "CHAVE_SIMETRICA " + this.simetricKeyCripto;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void encryptedMessage(String Message){
        try {
            Cipher cif = Cipher.getInstance("AES");
            cif.init(Cipher.ENCRYPT_MODE, this.key);

            byte[] buffer = cif.doFinal(Message.getBytes());
            String messageToSend = Base64.getEncoder().encodeToString(buffer);
            //envio messageToSend
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String decryptMessageFromClient(String message) throws Exception {
        try {
            byte[] messageBytes = Base64.getDecoder().decode(message);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, this.key);

            byte[] decryptedMessageBytes = cipher.doFinal(messageBytes);
            String decryptedMessage = new String(decryptedMessageBytes);

            return decryptedMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Failed to decrypt message";

        }
    }
}