package net.enhancem.mixin;

import net.enhancem.ShieldZombieAccess;
import net.enhancem.SwordZombieAccess;
import net.enhancem.SwordZombieAttackAnimation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ZombieShieldDisableMixin {

    @Inject(method = "blockUsingItem", at = @At("TAIL"))
    private void enhancem$disableZombieShield(ServerLevel level, LivingEntity attacker,
        DamageSource source, float damage, boolean fullyBlocked, CallbackInfo ci) {
        if (!((Object) this instanceof Zombie zombie) || !(attacker instanceof Player)
            || !((ShieldZombieAccess) zombie).enhancem$isShieldZombie()
            || zombie.getUsedItemHand() != InteractionHand.OFF_HAND) {
            return;
        }

        ItemStack shield = zombie.getItemBlockingWith();
        if (shield == null || !shield.is(Items.SHIELD)) {
            return;
        }
        BlocksAttacks blocking = shield.get(DataComponents.BLOCKS_ATTACKS);
        float seconds = attacker.getSecondsToDisableBlocking();
        if (blocking == null || seconds <= 0.0F) {
            return;
        }
        int ticks = Math.round(seconds * blocking.disableCooldownScale() * 20.0F);
        if (ticks <= 0) {
            return;
        }

        // Mobs need their own cooldown; vanilla only stores item cooldowns for players.
        ((ShieldZombieAccess) zombie).enhancem$disableShield(ticks);
        // Vanilla stops item use and broadcasts the shield's configured disable sound.
        blocking.disable(level, zombie, seconds, shield);
        SwordZombieAccess swordZombie = (SwordZombieAccess) zombie;
        if (swordZombie.enhancem$getSwordAttackPhase() == SwordZombieAttackAnimation.PHASE_GUARDED_THRUST) {
            swordZombie.enhancem$setSwordAttackPhase(SwordZombieAttackAnimation.PHASE_NONE);
        }
    }
}
