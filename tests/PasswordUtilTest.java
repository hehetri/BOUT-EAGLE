package LoginServer;

public class PasswordUtilTest {
	public static void main(String[] args)
	{
		String user = "player";
		String pass = "Secret123";
		if (!PasswordUtil.isCredentialInputValid(user, pass))
			throw new RuntimeException("Expected valid credentials to pass validation.");
		String hash = PasswordUtil.hashPassword(pass);
		if (!PasswordUtil.verifyPassword(pass, hash))
			throw new RuntimeException("Hashed password verification failed.");
		if (PasswordUtil.verifyPassword("wrong", hash))
			throw new RuntimeException("Invalid password should not verify.");
		if (PasswordUtil.shouldUpgradePassword(hash))
			throw new RuntimeException("Hashed password should not require upgrade.");
		String legacy = "plaintext";
		if (!PasswordUtil.verifyPassword("plaintext", legacy))
			throw new RuntimeException("Legacy plaintext password verification failed.");
		if (!PasswordUtil.shouldUpgradePassword(legacy))
			throw new RuntimeException("Legacy plaintext password should require upgrade.");
	}
}
