package net.enhancem;

/** Navigation speed multiplier used only while the melee pursuit goal is running. */
public interface ChaseSpeedAccess {
    // Vanilla Vindicator: movement attribute 0.35F, melee navigation multiplier 1.0.
    double VINDICATOR_CHASE_SPEED = 0.35F;

    double enhancem$getChaseSpeedMultiplier();
}
