package net.enhancem;

public final class SwordZombieAttackAnimation {

	public static final int PHASE_NONE = 0;
	public static final int PHASE_FIRST = 1;
	public static final int PHASE_SECOND = 2;
	public static final int FIRST_PHASE_DURATION_TICKS = 10;
	public static final int SECOND_PHASE_DURATION_TICKS = 10;
	public static final int FIRST_PHASE_DAMAGE_TICK = 7;
	public static final int SECOND_PHASE_DAMAGE_TICK = 7;
	public static final int TOTAL_COMBO_DURATION_TICKS = FIRST_PHASE_DURATION_TICKS + SECOND_PHASE_DURATION_TICKS;

	private SwordZombieAttackAnimation() {
	}

	public static int getPhaseDurationTicks(int phase) {
		return switch (phase) {
			case PHASE_FIRST -> FIRST_PHASE_DURATION_TICKS;
			case PHASE_SECOND -> SECOND_PHASE_DURATION_TICKS;
			default -> 0;
		};
	}

	public static int getPhaseDamageTick(int phase) {
		return switch (phase) {
			case PHASE_FIRST -> FIRST_PHASE_DAMAGE_TICK;
			case PHASE_SECOND -> SECOND_PHASE_DAMAGE_TICK;
			default -> 0;
		};
	}
}
