package doodlejump;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Account {
	protected String username;
	protected String passwordHash;
	protected AccountType type;

	protected Account(String username, String password, AccountType type) {
		this.username = username;
		this.passwordHash = hashPassword(password);
		this.type = type;
	}

	protected Account(String username, String passwordHash, AccountType type, boolean isHash) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public AccountType getType() {
		return type;
	}

	public boolean verifyPassword(String password) {
		return passwordHash.equals(hashPassword(password));
	}

	public abstract String serialize();

	public static Account deserialize(String line) {
		if (line == null || line.isBlank()) {
			return null;
		}

		String[] parts = line.split("\\|", -1);
		if (parts.length < 3) {
			return null;
		}

		String typePart = parts[0];
		String username = parts[1];
		String storedHash = parts[2];

		try {
			AccountType type = AccountType.valueOf(typePart);
			switch (type) {
				case PLAYER:
					int bestScore = parts.length > 3 && !parts[3].isBlank() ? Integer.parseInt(parts[3]) : 0;
					return new PlayerAccount(username, storedHash, bestScore, true);
				case USER:
					String email = parts.length > 3 ? parts[3] : "";
					String role = parts.length > 4 ? parts[4] : "user";
					return new UserAccount(username, storedHash, email, role, true);
				default:
					return null;
			}
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : bytes) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			return Integer.toHexString(password.hashCode());
		}
	}
}
