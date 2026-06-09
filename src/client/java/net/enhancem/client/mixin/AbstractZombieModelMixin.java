package net.enhancem.client.mixin;

import net.enhancem.SwordZombieAttackAnimation;
import net.enhancem.client.SwordZombieRenderStateAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.AbstractZombieModel;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin {

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;)V", at = @At("TAIL"))
	private void enhancem$applySwordComboPose(ZombieRenderState renderState, CallbackInfo ci) {
		if (!renderState.getMainHandItemStack().is(ItemTags.SWORDS)) {
			return;
		}

		SwordZombieRenderStateAccess swordState = (SwordZombieRenderStateAccess) renderState;
		int phase = swordState.enhancem$getSwordAttackPhase();

		@SuppressWarnings("unchecked")
		HumanoidModel<ZombieRenderState> model = (HumanoidModel<ZombieRenderState>) (Object) this;

		if (phase == SwordZombieAttackAnimation.PHASE_NONE) {
			model.body.z = 0.0F;
			model.body.yRot = 0.0F;
			model.leftArm.z = 0.0F;
			return;
		}

		if (renderState.mainArm == HumanoidArm.LEFT) {
			return;
		}

		float progress = swordState.enhancem$getSwordAttackProgress();
		if (phase == SwordZombieAttackAnimation.PHASE_FIRST) {
			this.enhancem$applyFirstPhase(model, progress);
		} else {
			this.enhancem$applySecondPhase(model, progress);
		}
	}

	@Unique
	private static final float[] ARM_REST = {-90.0F, 0.0F, 0.0F};
	@Unique
	private static final float[] LEG_REST = {0.0F, 0.0F, 0.0F};
	@Unique
	private static final float[] LA_HELD = {13.6501F, -2.1587F, -13.3894F};
	@Unique
	private static final float[] RL_HELD = {-19.2895F, -6.5135F, 11.2243F};
	@Unique
	private static final float[] LL_HELD = {9.5398F, -14.9418F, 0.4992F};
	@Unique
	private static final float[] P1_RA_MID = {-127.0847F, 0.1433F, 25.3107F};
	@Unique
	private static final float[] P1_RA_END = {-24.5847F, 0.1433F, 25.3107F};
	@Unique
	private static final float[] P2_RA_MID = {-140.9207F, -43.8404F, 132.9151F};
	@Unique
	private static final float[] P2_RA_END = {-37.6417F, -36.5126F, 123.9862F};

	@Unique
	private void enhancem$applyFirstPhase(HumanoidModel<ZombieRenderState> model, float progress) {
		float windupEnd = 0.52F;
		float strikeEnd = 0.76F;
		if (progress <= windupEnd) {
			float t = progress / windupEnd;
			this.enhancem$lerpPose(model, t,
				ARM_REST, P1_RA_MID, ARM_REST, LA_HELD, LEG_REST, RL_HELD, LEG_REST, LL_HELD,
				0.0F, -22.5F, 0.0F, 1.0F);
			return;
		}

		float t = progress <= strikeEnd ? (progress - windupEnd) / (strikeEnd - windupEnd) : 1.0F;
		this.enhancem$lerpPose(model, t,
			P1_RA_MID, P1_RA_END, LA_HELD, LA_HELD, RL_HELD, RL_HELD, LL_HELD, LL_HELD,
			-22.5F, -22.5F, 1.0F, 1.0F);
	}

	@Unique
	private void enhancem$applySecondPhase(HumanoidModel<ZombieRenderState> model, float progress) {
		float windupEnd = 0.5F;
		float strikeEnd = 0.75F;
		if (progress <= windupEnd) {
			float t = progress / windupEnd;
			this.enhancem$lerpPose(model, t,
				P1_RA_END, P2_RA_MID, LA_HELD, LA_HELD, RL_HELD, RL_HELD, LL_HELD, LL_HELD,
				-22.5F, -22.5F, 1.0F, 1.0F);
			return;
		}

		float t = progress <= strikeEnd ? (progress - windupEnd) / (strikeEnd - windupEnd) : 1.0F;
		this.enhancem$lerpPose(model, t,
			P2_RA_MID, P2_RA_END, LA_HELD, LA_HELD, RL_HELD, RL_HELD, LL_HELD, LL_HELD,
			-22.5F, -22.5F, 1.0F, 1.0F);
	}

	@Unique
	private void enhancem$lerpPose(
		HumanoidModel<ZombieRenderState> model, float t,
		float[] raFrom, float[] raTo,
		float[] laFrom, float[] laTo,
		float[] rlFrom, float[] rlTo,
		float[] llFrom, float[] llTo,
		float bodyYRotFrom, float bodyYRotTo,
		float laZFrom, float laZTo
	) {
		this.enhancem$applyRot(model.rightArm, t, raFrom, raTo);
		this.enhancem$applyRot(model.leftArm, t, laFrom, laTo);
		this.enhancem$applyRot(model.rightLeg, t, rlFrom, rlTo);
		this.enhancem$applyRot(model.leftLeg, t, llFrom, llTo);
		model.body.yRot = enhancem$rad(Mth.lerp(t, bodyYRotFrom, bodyYRotTo));
		model.leftArm.z = Mth.lerp(t, laZFrom, laZTo);
	}

	@Unique
	private void enhancem$applyRot(ModelPart part, float t, float[] from, float[] to) {
		part.xRot = enhancem$rad(Mth.lerp(t, from[0], to[0]));
		part.yRot = enhancem$rad(Mth.lerp(t, from[1], to[1]));
		part.zRot = enhancem$rad(Mth.lerp(t, from[2], to[2]));
	}

	@Unique
	private static float enhancem$rad(float degrees) {
		return degrees * (float) Math.PI / 180.0F;
	}
}
