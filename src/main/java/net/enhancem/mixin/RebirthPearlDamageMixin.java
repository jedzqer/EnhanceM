package net.enhancem.mixin;

import net.enhancem.item.RebirthPearl;
import net.enhancem.network.RebirthPearlChannelPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class RebirthPearlDamageMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void enhancem$cancelRebirthOnDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer serverPlayer)) return;
        if (!RebirthPearl.CHANNELING.containsKey(serverPlayer.getUUID())) return;

        RebirthPearl.CHANNELING.remove(serverPlayer.getUUID());
        ServerPlayNetworking.send(serverPlayer, new RebirthPearlChannelPayload(false));
        serverPlayer.sendOverlayMessage(
                Component.translatable("item.enhancem.rebirth_pearl.interrupted"));
    }
}
