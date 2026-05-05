package net.enhancem.mixin;

import net.enhancem.ShieldZombieAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieAttackGoal.class)
public abstract class ZombieShieldAttackGoalMixin {

    @Shadow
    @Final
    private Zombie zombie;

    @Shadow
    private int raiseArmTicks;

    @Inject(method = "tick", at = @At("TAIL"))
    private void enhancem$suppressWindupWhileBlocking(CallbackInfo ci) {
        if (this.zombie.level().isClientSide() || !((ShieldZombieAccess) this.zombie).enhancem$isShieldZombie()) {
            return;
        }

        if (this.zombie.isUsingItem() && this.zombie.getUsedItemHand() == InteractionHand.OFF_HAND) {
            this.raiseArmTicks = 0;
            this.zombie.setAggressive(false);
        }
    }

    @Inject(method = "stop", at = @At("TAIL"))
    private void enhancem$stopBlockingWhenGoalStops(CallbackInfo ci) {
        this.enhancem$stopBlocking();
    }

    @Unique
    private void enhancem$stopBlocking() {
        if (this.zombie.isUsingItem() && this.zombie.getUsedItemHand() == InteractionHand.OFF_HAND) {
            this.zombie.stopUsingItem();
        }
    }
}
