package Tool;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import javax.crypto.Cipher;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.io.*;

public class RSAGen {
	public void rsaGen() {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(1024);
			KeyPair keyPair = gen.generateKeyPair();
			PublicKey publickey = keyPair.getPublic();
			PrivateKey privateKey = keyPair.getPrivate();
			
			RSAPublicKey rsaPublicKey = (RSAPublicKey)publickey;
			RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)privateKey;
			
			BigInteger n = rsaPublicKey.getModulus();
			BigInteger e = rsaPublicKey.getPublicExponent();
			BigInteger d = rsaPrivateKey.getPrivateExponent();
			
			FileWriter fw = new FileWriter("pubkey");
			fw.write(n+"\n");
			fw.write(d+"\n");
			fw.close();
			
			fw = new FileWriter("secretkey");
			fw.write(n+"\n");
			fw.write(e+"\n");
			fw.close();
		}catch(Exception excep) {excep.printStackTrace();}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RSAGen gen = new RSAGen();
		gen.rsaGen();
	}

}
