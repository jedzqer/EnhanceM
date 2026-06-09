package net.enhancem.client.mixin;

import net.enhancem.SwordZombieAccess;
import net.enhancem.client.SwordZombieRenderStateAccess;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieRenderer.class)
public abstract class AbstractZombieRendererMixin {

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/zombie/Zombie;Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;F)V", at = @At("TAIL"))
	private void enhancem$extractSwordAttackState(Zombie zombie, ZombieRenderState renderState, float partialTick, CallbackInfo ci) {
		SwordZombieAccess swordZombie = (SwordZombieAccess)zombie;
		SwordZombieRenderStateAccess swordRenderState = (SwordZombieRenderStateAccess)renderState;
		swordRenderState.enhancem$setSwordAttackPhase(swordZombie.enhancem$getSwordAttackPhase());
		swordRenderState.enhancem$setSwordAttackProgress(swordZombie.enhancem$getSwordAttackAnimationProgress(partialTick));
	}
}
