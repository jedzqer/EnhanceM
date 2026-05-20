package net.enhancem.mixin;

import net.enhancem.EnhanceM;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class SpeedBootsPlayerMixin {

	@Unique
	private static final Identifier ENHANCEM_SPEED_BOOTS_MODIFIER_ID = Identifier.fromNamespaceAndPath(
			EnhanceM.MOD_ID,
			"speed_boots_land_sprint"
	);
	@Unique
	private static final double ENHANCEM_SPEED_BOOTS_SPEED_BONUS = 0.7D;
	@Unique
	private static final float ENHANCEM_SPEED_BOOTS_EXTRA_EXHAUSTION_PER_BLOCK = 0.1F;

	@Unique
	private double enhancem$lastTrackedX;
	@Unique
	private double enhancem$lastTrackedZ;
	@Unique
	private boolean enhancem$trackingInitialized;

	@Inject(method = "travel", at = @At("HEAD"))
	private void enhancem$updateSpeedModifier(Vec3 travelVector, CallbackInfo ci) {
		Player player = (Player) (Object) this;
		AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speedAttr == null) {
			return;
		}

		boolean shouldBoost = enhancem$wearsSpeedBoots(player) && enhancem$isLandSprinting(player);
		if (shouldBoost) {
			if (!speedAttr.hasModifier(ENHANCEM_SPEED_BOOTS_MODIFIER_ID)) {
				speedAttr.addTransientModifier(new AttributeModifier(
						ENHANCEM_SPEED_BOOTS_MODIFIER_ID,
						ENHANCEM_SPEED_BOOTS_SPEED_BONUS,
						AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
				));
			}
			return;
		}

		speedAttr.removeModifier(ENHANCEM_SPEED_BOOTS_MODIFIER_ID);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$applyExtraSprintExhaustion(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (!this.enhancem$trackingInitialized) {
			this.enhancem$lastTrackedX = player.getX();
			this.enhancem$lastTrackedZ = player.getZ();
			this.enhancem$trackingInitialized = true;
			return;
		}

		if (!player.level().isClientSide() && enhancem$wearsSpeedBoots(player) && enhancem$isLandSprinting(player)) {
			double deltaX = player.getX() - this.enhancem$lastTrackedX;
			double deltaZ = player.getZ() - this.enhancem$lastTrackedZ;
			double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

			if (horizontalDistance > 0.0D) {
				player.causeFoodExhaustion((float) (horizontalDistance * ENHANCEM_SPEED_BOOTS_EXTRA_EXHAUSTION_PER_BLOCK));
			}
		}

		this.enhancem$lastTrackedX = player.getX();
		this.enhancem$lastTrackedZ = player.getZ();
	}

	@Unique
	private static boolean enhancem$wearsSpeedBoots(Player player) {
		ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
		return boots.is(EnhanceM.SPEED_BOOTS);
	}

	@Unique
	private static boolean enhancem$isLandSprinting(Player player) {
		return player.isSprinting()
				&& player.onGround()
				&& !player.isPassenger()
				&& !player.isInLiquid()
				&& !player.isSwimming()
				&& !player.isFallFlying();
	}
}
