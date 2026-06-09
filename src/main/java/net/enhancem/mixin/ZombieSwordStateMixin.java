package net.enhancem.mixin;

import net.enhancem.SwordZombieAccess;
import net.enhancem.SwordZombieAttackAnimation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public abstract class ZombieSwordStateMixin implements SwordZombieAccess {

	@Unique
	private static final EntityDataAccessor<Byte> ENHANCEM_SWORD_ATTACK_PHASE = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BYTE);

	@Unique
	private long enhancem$swordAttackPhaseStartGameTime;

	@Unique
	private int enhancem$lastSwordAttackPhase;

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void enhancem$defineSwordAttackPhase(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(ENHANCEM_SWORD_ATTACK_PHASE, (byte)SwordZombieAttackAnimation.PHASE_NONE);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void enhancem$trackSwordAttackPhaseChanges(CallbackInfo ci) {
		int phase = this.enhancem$getSwordAttackPhase();
		if (phase != this.enhancem$lastSwordAttackPhase) {
			this.enhancem$markSwordAttackPhaseStart();
			this.enhancem$lastSwordAttackPhase = phase;
		}
	}

	@Override
	public int enhancem$getSwordAttackPhase() {
		Zombie zombie = (Zombie)(Object)this;
		if (!zombie.getMainHandItem().is(ItemTags.SWORDS)
			|| zombie.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD)) {
			return SwordZombieAttackAnimation.PHASE_NONE;
		}

		return zombie.getEntityData().get(ENHANCEM_SWORD_ATTACK_PHASE);
	}

	@Override
	public void enhancem$setSwordAttackPhase(int phase) {
		Zombie zombie = (Zombie)(Object)this;
		zombie.getEntityData().set(ENHANCEM_SWORD_ATTACK_PHASE, (byte)phase);
		this.enhancem$lastSwordAttackPhase = phase;
		this.enhancem$markSwordAttackPhaseStart();
	}

	@Override
	public float enhancem$getSwordAttackAnimationProgress(float partialTick) {
		Zombie zombie = (Zombie)(Object)this;
		int phase = this.enhancem$getSwordAttackPhase();
		int duration = SwordZombieAttackAnimation.getPhaseDurationTicks(phase);
		if (duration <= 0) {
			return 0.0F;
		}

		Level level = zombie.level();
		float elapsedTicks = (float)(level.getGameTime() - this.enhancem$swordAttackPhaseStartGameTime) + partialTick;
		return Math.clamp(elapsedTicks / (float)duration, 0.0F, 1.0F);
	}

	@Unique
	private void enhancem$markSwordAttackPhaseStart() {
		Zombie zombie = (Zombie)(Object)this;
		this.enhancem$swordAttackPhaseStartGameTime = zombie.level().getGameTime();
	}
}
