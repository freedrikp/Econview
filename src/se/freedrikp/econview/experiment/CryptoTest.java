package se.freedrikp.econview.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoTest {

	public static void main(String[] args) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] digest = md.digest(("fredrik").getBytes());
			SecretKeySpec kspec = new SecretKeySpec(Arrays.copyOf(digest, 16),
					"AES");
			IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(digest,
					16, 32));

			c.init(Cipher.ENCRYPT_MODE, kspec, iv);
			String s = "thisistopsecretstuff";
			System.out.println("Before encryption: " + s);
			byte[] encrypted = c.doFinal(s.getBytes("UTF-8"));
			System.out.println("After encryption: "
					+ (new String(encrypted, "UTF-8")));
			// c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, kspec, iv);
			byte[] decrypted = c.doFinal(encrypted);
			System.out.println("After decryption: "
					+ (new String(decrypted, "UTF-8")));

			// c.init(Cipher.ENCRYPT_MODE, kspec, iv);
			// File secret = new File("secret.txt");
			// FileInputStream fis = new FileInputStream(secret);
			// byte[] buffer = new byte[(int) secret.length()];
			// int read = 0;
			// int total = 0;
			// while (total < secret.length()) {
			// read = fis.read(buffer, total, buffer.length-total);
			// total += read;
			// }
			// fis.close();
			// encrypted = c.doFinal(buffer);
			// FileOutputStream fos = new FileOutputStream("encrypted.txt");
			// fos.write(encrypted);
			// fos.flush();
			// fos.close();
			//
			// c.init(Cipher.DECRYPT_MODE, kspec, iv);
			// File enc = new File("encrypted.txt");
			// fis = new FileInputStream(enc);
			// buffer = new byte[(int) enc.length()];
			// read = 0;
			// total = 0;
			// while (total < enc.length()) {
			// read = fis.read(buffer, total, buffer.length-total);
			// total += read;
			// }
			// fis.close();
			// decrypted = c.doFinal(buffer);
			// fos = new FileOutputStream("decrypted.txt");
			// fos.write(decrypted);
			// fos.flush();
			// fos.close();

			c.init(Cipher.ENCRYPT_MODE, kspec, iv);
			File secret = new File("secret.txt");
			FileInputStream fis = new FileInputStream(secret);
			CipherOutputStream cos = new CipherOutputStream(
					new FileOutputStream("encrypted.txt"), c);
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = fis.read(buffer)) > -1) {
				cos.write(buffer, 0, read);
			}
			fis.close();
			cos.flush();
			cos.close();

			c.init(Cipher.DECRYPT_MODE, kspec, iv);
			File enc = new File("encrypted.txt");
			CipherInputStream cis = new CipherInputStream(new FileInputStream(
					enc), c);
			FileOutputStream fos = new FileOutputStream("decrypted.txt");
			while ((read = cis.read(buffer)) > -1) {
				fos.write(buffer, 0, read);
			}
			cis.close();
			fos.flush();
			fos.close();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
