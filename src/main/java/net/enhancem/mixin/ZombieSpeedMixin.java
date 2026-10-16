package net.enhancem.mixin;

import net.enhancem.ChaseSpeedAccess;
import net.enhancem.ShieldZombieAccess;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public class ZombieSpeedMixin implements ChaseSpeedAccess {

	@Unique
	private static final double ZOMBIE_SPEED = 0.35;

	@Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
	private void enhancem$modifySpeed(CallbackInfo ci) {
		Zombie zombie = (Zombie) (Object) this;
		if (zombie instanceof Drowned || this.enhancem$isOrdinaryZombie(zombie)) {
			return;
		}

		AttributeInstance speedAttr = zombie.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speedAttr != null) {
			speedAttr.setBaseValue(ZOMBIE_SPEED);
		}
	}
    @Unique
    private boolean enhancem$isOrdinaryZombie(Zombie zombie) {
        return zombie.getType() == EntityTypes.ZOMBIE
            || zombie.getType() == EntityTypes.HUSK
            || zombie.getType() == EntityTypes.ZOMBIE_VILLAGER;
    }

    @Override
    public double enhancem$getChaseSpeedMultiplier() {
        Zombie zombie = (Zombie) (Object) this;
        return this.enhancem$isOrdinaryZombie(zombie)
            && !((ShieldZombieAccess) zombie).enhancem$isShieldZombie()
            ? VINDICATOR_CHASE_SPEED / 0.23D : 1.0D;
    }
}
