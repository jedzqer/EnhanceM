package net.enhancem.mixin;

import net.enhancem.ShieldZombieAccess;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieShieldMixin implements ShieldZombieAccess {

    @Unique
    private static final String SHIELD_ZOMBIE_TAG = "EnhanceMShieldZombie";
    @Unique
    private static final float SHIELD_ZOMBIE_CHANCE = 0.25F;
    @Unique
    private static final double SHIELD_ZOMBIE_SPEED = 0.23;
    @Unique
    private static final double ENHANCED_ZOMBIE_SPEED = 0.35;

    @Unique
    private boolean enhancem$shieldZombie;

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void enhancem$equipShieldOnSpawn(
        ServerLevelAccessor level,
        DifficultyInstance difficulty,
        EntitySpawnReason spawnReason,
        SpawnGroupData groupData,
        CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        Zombie zombie = (Zombie) (Object) this;
        if (zombie.getRandom().nextFloat() < SHIELD_ZOMBIE_CHANCE) {
            this.enhancem$shieldZombie = true;
            zombie.setLeftHanded(false);
            zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        }

        this.enhancem$applyMovementSpeed(zombie);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void enhancem$saveShieldZombie(ValueOutput output, CallbackInfo ci) {
        output.putBoolean(SHIELD_ZOMBIE_TAG, this.enhancem$shieldZombie);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void enhancem$loadShieldZombie(ValueInput input, CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;
        this.enhancem$shieldZombie = input.getBooleanOr(SHIELD_ZOMBIE_TAG, false);
        this.enhancem$applyMovementSpeed(zombie);
    }

    @Unique
    private void enhancem$applyMovementSpeed(Zombie zombie) {
        AttributeInstance speedAttr = zombie.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(this.enhancem$shieldZombie ? SHIELD_ZOMBIE_SPEED : ENHANCED_ZOMBIE_SPEED);
        }
    }

    @Override
    public boolean enhancem$isShieldZombie() {
        return this.enhancem$shieldZombie;
    }
}
