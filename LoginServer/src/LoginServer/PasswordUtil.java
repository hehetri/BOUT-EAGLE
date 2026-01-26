package LoginServer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
	private static final String HASH_PREFIX = "pbkdf2$";
	private static final int HASH_ITERATIONS = 120000;
	private static final int HASH_KEY_LENGTH = 256;
	private static final int HASH_SALT_BYTES = 16;

	private PasswordUtil() {}

	public static boolean isCredentialInputValid(String user, String pass)
	{
		if (user == null || pass == null)
			return false;
		if (user.trim().isEmpty() || pass.isEmpty())
			return false;
		if (user.length() > 23 || pass.length() > 32)
			return false;
		return true;
	}

	public static boolean verifyPassword(String password, String storedHash)
	{
		if (storedHash == null)
			return false;
		if (storedHash.startsWith(HASH_PREFIX))
			return verifyPbkdf2(password, storedHash);
		if (isMd5Hash(storedHash))
			return MessageDigest.isEqual(md5Bytes(password), decodeHex(storedHash));
		return MessageDigest.isEqual(storedHash.getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8));
	}

	public static boolean shouldUpgradePassword(String storedHash)
	{
		return storedHash == null || !storedHash.startsWith(HASH_PREFIX);
	}

	public static String hashPassword(String password)
	{
		byte[] salt = new byte[HASH_SALT_BYTES];
		new SecureRandom().nextBytes(salt);
		byte[] hash = pbkdf2(password.toCharArray(), salt, HASH_ITERATIONS, HASH_KEY_LENGTH);
		return HASH_PREFIX + HASH_ITERATIONS + "$" +
				Base64.getEncoder().encodeToString(salt) + "$" +
				Base64.getEncoder().encodeToString(hash);
	}

	private static boolean verifyPbkdf2(String password, String storedHash)
	{
		String[] parts = storedHash.split("\\$");
		if (parts.length != 4)
			return false;
		int iterations;
		try {
			iterations = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			return false;
		}
		byte[] salt = Base64.getDecoder().decode(parts[2]);
		byte[] expected = Base64.getDecoder().decode(parts[3]);
		byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
		return MessageDigest.isEqual(expected, actual);
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
	{
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			return skf.generateSecret(spec).getEncoded();
		} catch (Exception e) {
			return new byte[0];
		}
	}

	private static boolean isMd5Hash(String storedHash)
	{
		return storedHash.matches("(?i)[0-9a-f]{32}");
	}

	private static byte[] md5Bytes(String input)
	{
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(input.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return new byte[0];
		}
	}

	private static byte[] decodeHex(String hex)
	{
		int len = hex.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
					+ Character.digit(hex.charAt(i+1), 16));
		}
		return data;
	}
}
