package kin.backup_and_restore;

public class Validator {

	public static void checkNotNull(Object object, String paramName) {
		if (object == null) {
			throw new IllegalArgumentException(paramName + " is null");
		}
	}

}
