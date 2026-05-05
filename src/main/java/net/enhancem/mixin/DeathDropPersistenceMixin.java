package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class DeathDropPersistenceMixin {

	@Unique
	private int enhancem$deathDropDepth;

	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void enhancem$beginDeathDrop(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
		if (!this.enhancem$isPlayer()) {
			return;
		}

		this.enhancem$deathDropDepth++;
	}

	@Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
	private void enhancem$endDeathDrop(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
		if (!this.enhancem$isPlayer()) {
			return;
		}

		if (this.enhancem$deathDropDepth > 0) {
			this.enhancem$deathDropDepth--;
		}
	}

	@Inject(
		method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At("RETURN")
	)
	private void enhancem$keepDeathDrops(ServerLevel level, ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {
		this.enhancem$setUnlimitedLifetime(cir);
	}

	@Inject(
		method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At("RETURN")
	)
	private void enhancem$keepDeathDrops(ServerLevel level, ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
		this.enhancem$setUnlimitedLifetime(cir);
	}

	@Inject(
		method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At("RETURN")
	)
	private void enhancem$keepDeathDrops(ServerLevel level, ItemStack stack, Vec3 position, CallbackInfoReturnable<ItemEntity> cir) {
		this.enhancem$setUnlimitedLifetime(cir);
	}

	@Unique
	private void enhancem$setUnlimitedLifetime(CallbackInfoReturnable<ItemEntity> cir) {
		if (this.enhancem$deathDropDepth <= 0) {
			return;
		}

		ItemEntity itemEntity = cir.getReturnValue();
		if (itemEntity != null) {
			itemEntity.setUnlimitedLifetime();
		}
	}

	@Unique
	private boolean enhancem$isPlayer() {
		return (Object) this instanceof Player;
	}
}
