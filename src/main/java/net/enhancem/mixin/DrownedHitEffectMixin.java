package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DrownedHitEffectMixin {

	@Unique
	private static final int ENHANCEM_AIR_REDUCTION = 150;

	@Inject(method = "hurtServer", at = @At("TAIL"))
	private void enhancem$reduceAirOnDrownedHit(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity target = (LivingEntity) (Object) this;
		if (!cir.getReturnValue() || !(target instanceof Player player) || !player.isAlive()) {
			return;
		}

		if (!(source.getEntity() instanceof Drowned drowned) || source.getDirectEntity() != drowned) {
			return;
		}

		player.setAirSupply(player.getAirSupply() - ENHANCEM_AIR_REDUCTION);
	}
}
