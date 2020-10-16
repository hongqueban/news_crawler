package com.ustcinfo.hftnews.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

/**
 * 信息加密(独创)
 * @author Joss
 *
 */
public class Key64 {
	
	private static final char[] KEYS = { 'a', 'A', 'b', 'B', 'c', 'C', '0', '1', '2', '3', '4', '5', 'd', 'D', 'e', 'E',
			'f', 'F', 'g', 'G', '6', '7', '8', '9', 'h', 'H', 'i', 'I', 'j', 'J', 'k', 'K', 'l', 'L', 'M', '=', '/',
			'm', 'N', 'n', 'o', 'O', 'P', 'p', 'q', 'Q', 'r', 'R', 's', 'S', 'T', 't', 'u', 'U', 'v', 'V', 'W', 'w',
			'X', 'x', 'Y', 'y', 'z', 'Z' };
	
	private static final char[] ENCRYPT_KEYS = { 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h',
			'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '2', '4', '6', '8', '0', '1', '3', '5', '7', '9', 'Z',
			'X', 'N', 'C', 'V', 'B', 'P', '=', 'M', '/', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Q', 'W', 'E',
			'R', 'T', 'Y', 'U', 'I', 'O' };
	
	private String charsetName;
	private String key;
	private String prefix = "10000000";
	private static int num = 24;

	public String getCharsetName() {
		return charsetName;
	}
	
	public static void main(String[] args) {
		System.out.println(decipher("VFJydslcCH1xNV55X4KUvubeDiJ+kwVchxF2NiuTZuWsxXOshoXF32lem3QFfn2yinhqFDve2YCPeGM3NaH3H+YLTLIW53XMiSWy/0pfMRQjhk7L8BQgZPLg2w+7QI1nH8eL77vCpgkWJq9EKJVFSDC85haUGWYNY5iyrDRSbENcHOhi7s29r4RPefgpy4ct+DSSNk86ZTcdegCZrnwKQwkA"));
	}
	
	/**
	 * 加密报文
	 * @return
	 */
	public static String encrypt(String info) {
		try {
			if(info == null)
				throw new NullPointerException();
			String newInfo = URLEncoder.encode(info,"UTF-8");
			String key2 = getKey(num);
			String encode = EncryptionDevice.execute(newInfo, key2);
			return addKeyToCiphertext(encryptKey(key2), encode);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);			
		}
	}
	
	private static int getIndex(char ch , char[] args) {
		int length = args.length;
		for (int i = 0; i < length; i++) {
			if(args[i] == ch)
				return i;
		}
		return -1;
	}
	
	private static String encryptKey(String key) {
		char[] charArray = key.toCharArray();
		StringBuilder builder = new StringBuilder();
		for(char ch : charArray) {
			int index = getIndex(ch, KEYS);
			builder.append(ENCRYPT_KEYS[index]);
		}
		return builder.toString();
	}
	
	private static String decipherKey(String key) {
		char[] charArray = key.toCharArray();
		StringBuilder builder = new StringBuilder();
		for(char ch : charArray) {
			int index = getIndex(ch, ENCRYPT_KEYS);
			builder.append(KEYS[index]);
		}
		return builder.toString();
	}
	
	/**
	 * 解密
	 * @param ciphertext
	 * @return
	 */
	public static String decipher(String ciphertext) {
		try {
			if(ciphertext == null)
				throw new NullPointerException();
			String key2 = getKeyByCiphertext(ciphertext, num);
			String encode = Decryptor.execute(getRealCiphertext(ciphertext, num), decipherKey(key2));
			return URLDecoder.decode(encode, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);			
		}
	}
	
	public static String getKey(int num) {
		int len = KEYS.length;
		Random random = new Random();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < num; i++) {
			int nextInt = random.nextInt(99999);
			long nanoTime = System.nanoTime();
			long l = nanoTime + nextInt;
			int index = (int) (l % len);
			builder.append(KEYS[index]);
		}
		return builder.toString();
	}
	
	public static String addKeyToCiphertext(String key , String ciphertext) {
		int len = key.length();
		if(len <= 6) {
			throw new IllegalArgumentException("密钥太短，长度应该大于6");
		}
		int v = len/2;
		StringBuilder builder = new StringBuilder();
		String start = key.substring(0, v);
		String end = key.substring(v);
		builder.append(start);
		builder.append(ciphertext);
		builder.append(end);
		return builder.toString();
	}
	
	public static String getKeyByCiphertext(String ciphertext , int keyLenght) {
		int v = keyLenght/2;
		String start = ciphertext.substring(0,v);
		int i = keyLenght - v;
		int in = ciphertext.length()-i;
		return start + ciphertext.substring(in);
	}
	
	public static String getRealCiphertext(String ciphertext , int keyLenght) {
		int v = keyLenght/2;
		String start = ciphertext.substring(0,v);
		int i = keyLenght - v;
		int in = ciphertext.length()-i;
		return ciphertext.substring(start.length(),in);
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}
	

	public String getBinaryString(String info) throws UnsupportedEncodingException {
		if (info == null || info.trim().isEmpty())
			throw new IllegalArgumentException("信息为空");
		int len = info.length();
		StringBuilder builder = new StringBuilder(prefix);
		for (int i = 0; i < len; i++) {
			String string = info.substring(i, i + 1);
			byte[] bytes = null;
			if (charsetName != null && !charsetName.trim().isEmpty()) {
				bytes = string.getBytes(charsetName);
			} else {
				bytes = string.getBytes();
			}
			for (byte b : bytes) {
				builder.append(getBinaryString(b));
			}
		}
		return builder.toString();
	}
	
	public String getStringByBinaryString(String binaryString) throws UnsupportedEncodingException {
		if (binaryString == null || binaryString.trim().isEmpty() || !binaryString.startsWith(prefix))
			throw new IllegalArgumentException("无法编译出该二进制文件");
		binaryString = binaryString.substring(prefix.length());
		int len = binaryString.length() / 8;
		byte[] bytes = new byte[len];
		for (int i = 0; i < len; i++) {
			String string = binaryString.substring(i * 8, (i + 1) * 8);
			byte b = getByte(string);
			bytes[i] = b;
		}
		if (charsetName != null && !charsetName.trim().isEmpty()) {
			return new String(bytes, charsetName);
		} else {
			return new String(bytes);
		}
	}

	private void install(byte b, int[] iArray, int index) {
		if (index == 0)
			return;
		iArray[index] = b % 2;
		index--;
		install((byte) (b / 2), iArray, index);
	}

	public String getBinaryString(byte b) {
		if (b == 0)
			return "00000000";
		int[] iArray = new int[8];
		if (b < 0) {
			iArray[0] = 1;
		}
		if (b > 0) {
			iArray[0] = 0;
		}
		install((byte) Math.abs(b), iArray, 7);
		StringBuilder builder = new StringBuilder();
		for (int i : iArray) {
			builder.append(i);
		}
		return builder.toString();
	}

	private int getIndexByChar(String ch) {
		for (int i = 0; i < KEYS.length; i++) {
			if (ch.charAt(0) == KEYS[i])
				return i;
		}
		return -1;
	}

	public String unzip64(String string64) {
		int len = string64.length();
		BigInteger bigInteger = new BigInteger("0");
		BigInteger big64 = new BigInteger("64");
		for (int i = 0; i < len; i++) {
			String ch = string64.substring(i, i + 1);
			int num = getIndexByChar(ch);
			if (num == -1) {
				throw new IllegalArgumentException("不是该加密格式");
			} else {
				if (num > 0) {
					bigInteger = bigInteger.add(new BigInteger(String.valueOf(num)).multiply(big64.pow(len - 1 - i)));
				}
			}

		}
		return bigInteger.toString();
	}

	public String zip64(StringBuilder builder, BigInteger bigInteger) {
		if (bigInteger.toString().matches("([1-9]{1})||([1-5]{1}[0-9]{1})||(6[0-3]{1})")) {
			StringBuilder _bBuilder = new StringBuilder();
			_bBuilder.append(KEYS[bigInteger.intValue()]);
			_bBuilder.append(builder);
			return _bBuilder.toString();
		}
		BigInteger bigInteger2 = new BigInteger("64");
		BigInteger divide = bigInteger.divide(bigInteger2);
		BigInteger remainder = bigInteger.remainder(bigInteger2);
		StringBuilder _bBuilder = new StringBuilder();
		_bBuilder.append(KEYS[remainder.intValue()]);
		_bBuilder.append(builder);
		return zip64(_bBuilder, divide);
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}

	/**
	 * 解密
	 * @param encodeInfo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String decode(String encodeInfo) throws UnsupportedEncodingException {
		if (encodeInfo == null || encodeInfo.trim().isEmpty()) {
			throw new IllegalArgumentException("解密信息为空");
		}
		String unzip64 = unzip64(encodeInfo);
		BigInteger bigInteger = new BigInteger(unzip64);
		if (key != null && !key.trim().isEmpty()) {
			String binaryString2 = getBinaryString(key);
			BigInteger bigInteger2 = getBigInteger(binaryString2);
			bigInteger = bigInteger.subtract(bigInteger2);
		}
		String binaryString2 = getBinaryString(bigInteger);
		return getStringByBinaryString(binaryString2);
	}
	
	/**
	 * 加密
	 * @param info
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String encode(String info) throws UnsupportedEncodingException {
		String binaryString = getBinaryString(info);
		BigInteger bigInteger = getBigInteger(binaryString);
		if (key != null && !key.trim().isEmpty()) {
			String binaryString2 = getBinaryString(key);
			BigInteger bigInteger2 = getBigInteger(binaryString2);
			bigInteger = bigInteger.add(bigInteger2);
		}
		return zip64(new StringBuilder(), bigInteger);
	}

	private String unzip(StringBuilder builder, BigInteger bigInteger) {
		if ("1".equals(bigInteger.toString())) {
			StringBuilder _bBuilder = new StringBuilder();
			_bBuilder.append(1);
			_bBuilder.append(builder);
			return _bBuilder.toString();
		}
		BigInteger bigInteger2 = new BigInteger("2");
		BigInteger divide = bigInteger.divide(bigInteger2);
		BigInteger remainder = bigInteger.remainder(bigInteger2);
		StringBuilder _bBuilder = new StringBuilder();
		_bBuilder.append(remainder.toString());
		_bBuilder.append(builder);
		return unzip(_bBuilder, divide);
	}

	public String getBinaryString(BigInteger bigInteger) {
		return unzip(new StringBuilder(), bigInteger);
	}

	public BigInteger getBigInteger(String binaryString) {
		int index = 0;
		BigInteger bigInteger = new BigInteger("0");
		BigInteger big2 = new BigInteger("2");
		int len = binaryString.length();
		while (true) {
			if (index == len) {
				return bigInteger;
			}
			String num = binaryString.substring(index, index + 1);
			if ("1".equals(num)) {
				bigInteger = bigInteger.add(big2.pow(len - index - 1));
			}
			index++;
		}
	}

	public byte getByte(String binaryString) {
		String[] args = binaryString.substring(1).split("");
		int sum = 0;
		int len = args.length;
		for (int i = 0; i < len; i++) {
			String num = args[i];
			if ("1".equals(num)) {
				sum += (int) (Math.pow(2.0, len - 1 - i));
			}
		}
		if (binaryString.startsWith("1")) {
			return (byte) (-1 * sum);
		}
		return (byte) sum;
	}

}