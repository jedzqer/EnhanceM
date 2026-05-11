package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class CreeperSpeedMixin {

	@Unique
	private static final String ENHANCEM_NORMAL_CREEPER_TAG = "EnhanceMNormalSpeedCreeper";
	@Unique
	private static final float ENHANCEM_NORMAL_CREEPER_CHANCE = 0.5F;
	@Unique
	private static final double ENHANCEM_NORMAL_CREEPER_SPEED = 0.25D;
	@Unique
	private static final double ENHANCEM_FAST_CREEPER_SPEED = ENHANCEM_NORMAL_CREEPER_SPEED * 2.0D;
	@Unique
	private static final int ENHANCEM_DEBUFF_DURATION_TICKS = 100;
	@Unique
	private static final int ENHANCEM_BLINDNESS_AMPLIFIER = 0;
	@Unique
	private static final int ENHANCEM_NAUSEA_AMPLIFIER = 0;
	@Unique
	private static final int ENHANCEM_SLOWNESS_AMPLIFIER = 1;

	@Unique
	private boolean enhancem$normalSpeedCreeper;
	@Unique
	private boolean enhancem$variantInitialized;

	@Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
	private void enhancem$assignVariantOnConstruct(CallbackInfo ci) {
		Creeper creeper = (Creeper) (Object) this;
		if (!this.enhancem$variantInitialized) {
			this.enhancem$normalSpeedCreeper = creeper.getRandom().nextFloat() < ENHANCEM_NORMAL_CREEPER_CHANCE;
			this.enhancem$variantInitialized = true;
		}
		this.enhancem$applyMovementSpeed(creeper);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void enhancem$saveVariant(ValueOutput output, CallbackInfo ci) {
		output.putBoolean(ENHANCEM_NORMAL_CREEPER_TAG, this.enhancem$normalSpeedCreeper);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void enhancem$loadVariant(ValueInput input, CallbackInfo ci) {
		Creeper creeper = (Creeper) (Object) this;
		this.enhancem$normalSpeedCreeper = input.getBooleanOr(ENHANCEM_NORMAL_CREEPER_TAG, false);
		this.enhancem$variantInitialized = true;
		this.enhancem$applyMovementSpeed(creeper);
	}

	@Inject(method = "explodeCreeper", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;discard()V"))
	private void enhancem$applyExplosionDebuffs(CallbackInfo ci) {
		if (!this.enhancem$normalSpeedCreeper) {
			return;
		}

		Creeper creeper = (Creeper) (Object) this;
		if (!(creeper.level() instanceof ServerLevel level)) {
			return;
		}

		double radius = creeper.isPowered() ? 6.0D : 3.0D;
		AABB area = creeper.getBoundingBox().inflate(radius);
		for (Player player : level.getEntitiesOfClass(Player.class, area, Player::isAlive)) {
			if (player.distanceToSqr(creeper) > radius * radius) {
				continue;
			}

			player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ENHANCEM_DEBUFF_DURATION_TICKS, ENHANCEM_BLINDNESS_AMPLIFIER), creeper);
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, ENHANCEM_DEBUFF_DURATION_TICKS, ENHANCEM_NAUSEA_AMPLIFIER), creeper);
			player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ENHANCEM_DEBUFF_DURATION_TICKS, ENHANCEM_SLOWNESS_AMPLIFIER), creeper);
		}
	}

	@Unique
	private void enhancem$applyMovementSpeed(Creeper creeper) {
		AttributeInstance speedAttr = creeper.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speedAttr != null) {
			speedAttr.setBaseValue(this.enhancem$normalSpeedCreeper ? ENHANCEM_NORMAL_CREEPER_SPEED : ENHANCEM_FAST_CREEPER_SPEED);
		}
	}
}
