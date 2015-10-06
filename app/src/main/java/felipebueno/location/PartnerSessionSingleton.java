package felipebueno.location;

import sneer.android.PartnerSession;

public class PartnerSessionSingleton {

	private static PartnerSession INSTANCE;

	public static void setInstance(PartnerSession session) {
		check(INSTANCE == null && session != null);
		INSTANCE = session;
	}

	public static PartnerSession session() {
		return INSTANCE;
	}

	public static void check(boolean condition) {
		if (!condition)
			throw new IllegalStateException();
	}

}
