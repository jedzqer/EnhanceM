package net.enhancem.mixin;

public final class ExperienceDropContext {

	private static final ThreadLocal<Boolean> PLAYER_DEATH_XP = ThreadLocal.withInitial(() -> false);

	private ExperienceDropContext() {
	}

	public static void beginPlayerDeathXp() {
		PLAYER_DEATH_XP.set(true);
	}

	public static void endPlayerDeathXp() {
		PLAYER_DEATH_XP.set(false);
	}

	public static boolean isPlayerDeathXp() {
		return PLAYER_DEATH_XP.get();
	}
}
