package net.enhancem.client.mixin;

import net.enhancem.client.SwordZombieRenderStateAccess;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ZombieRenderState.class)
public class ZombieRenderStateMixin implements SwordZombieRenderStateAccess {

	@Unique
	private int enhancem$swordAttackPhase;
	@Unique
	private float enhancem$swordAttackProgress;

	@Override
	public int enhancem$getSwordAttackPhase() {
		return this.enhancem$swordAttackPhase;
	}

	@Override
	public void enhancem$setSwordAttackPhase(int phase) {
		this.enhancem$swordAttackPhase = phase;
	}

	@Override
	public float enhancem$getSwordAttackProgress() {
		return this.enhancem$swordAttackProgress;
	}

	@Override
	public void enhancem$setSwordAttackProgress(float progress) {
		this.enhancem$swordAttackProgress = progress;
	}
}
