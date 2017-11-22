package Tool;
import java.util.*;
import java.security.MessageDigest;
import java.math.*;
import java.io.*;

public class Sig {
	String path = "rsasig";
	String secretkey = "secretkey";
	public Sig(String p,String sk) {
		path = p;
		secretkey = sk;
	}
	public Sig() {
		
	}
	private int byteArrayToInt(byte[] b) {
		return b[3] & 0XFF |
			(b[2] & 0XFF) << 8 |
			(b[1] & 0XFF) << 16 |
			(b[0] & 0XFF) << 24;
	}
	
	public int digest(String s) {
		int result = 0;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			byte[] inputByteArray = s.getBytes("utf-8");
			messageDigest.update(inputByteArray);
			byte[] resultbyte = messageDigest.digest();
			result = byteArrayToInt(resultbyte);
		}catch(Exception e) {}
		return result;
	}
	
	public void condenseRSA(ArrayList<String> m) {
		BigInteger n = null;
		BigInteger e = null;
		try {
			FileReader fr = new FileReader(secretkey);
			BufferedReader br = new BufferedReader(fr);
			
			n = new BigInteger(br.readLine());
			e = new BigInteger(br.readLine());
			fr.close();
			br.close();
		}catch(IOException excep) {}
		BigInteger sum = new BigInteger("1");
		for(String s:m) {
			int num2 = digest(s);
			sum = sum.multiply(new BigInteger(""+num2)).mod(n);
		}
		
		BigInteger sig = sum.modPow(e, n);
		try {
			FileWriter fw = new FileWriter(path);
			fw.write(""+sig);
			fw.close();
		}catch(IOException excep) {}
	}
}
