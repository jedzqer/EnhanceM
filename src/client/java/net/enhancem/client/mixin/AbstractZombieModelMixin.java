package net.enhancem.client.mixin;

import net.enhancem.SwordZombieAttackAnimation;
import net.enhancem.client.SwordZombieRenderStateAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.AbstractZombieModel;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;
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

		if (phase == SwordZombieAttackAnimation.PHASE_GUARDED_THRUST && this.enhancem$isUsingShield(renderState)) {
			this.enhancem$applyGuardedThrust(model, renderState, swordState.enhancem$getSwordAttackProgress());
			return;
		}

		if (phase == SwordZombieAttackAnimation.PHASE_NONE || phase == SwordZombieAttackAnimation.PHASE_GUARDED_THRUST) {
			if (renderState.walkAnimationSpeed > 0.01F) {
				model.rightArm.xRot = 0.0F;
				model.rightArm.yRot = 0.0F;
				model.rightArm.zRot = 0.0F;
				model.leftArm.xRot = 0.0F;
				model.leftArm.yRot = 0.0F;
				model.leftArm.zRot = 0.0F;
				model.rightLeg.xRot = 0.0F;
				model.rightLeg.yRot = 0.0F;
				model.rightLeg.zRot = 0.0F;
				model.leftLeg.xRot = 0.0F;
				model.leftLeg.yRot = 0.0F;
				model.leftLeg.zRot = 0.0F;
				model.body.xRot = 0.0F;
				this.enhancem$applySwordWalkAnimation(model, renderState);
			} else {
				// 待机姿势：使用行走动画第一帧
				model.body.xRot = enhancem$rad(7.5F);
				model.body.yRot = 0.0F;
				model.body.z = 0.0F;

				model.rightArm.xRot = enhancem$rad(WALK_RA_REST[0]);
				model.rightArm.yRot = enhancem$rad(WALK_RA_REST[1]);
				model.rightArm.zRot = enhancem$rad(WALK_RA_REST[2]);

				model.leftArm.xRot = enhancem$rad(WALK_LA_REST[0]);
				model.leftArm.yRot = enhancem$rad(WALK_LA_REST[1]);
				model.leftArm.zRot = enhancem$rad(WALK_LA_REST[2]);
				model.leftArm.z = 0.0F;

				model.rightLeg.xRot = enhancem$rad(WALK_RL_REST[0]);
				model.rightLeg.yRot = enhancem$rad(WALK_RL_REST[1]);
				model.rightLeg.zRot = enhancem$rad(WALK_RL_REST[2]);

				model.leftLeg.xRot = enhancem$rad(WALK_LL_REST[0]);
				model.leftLeg.yRot = enhancem$rad(WALK_LL_REST[1]);
				model.leftLeg.zRot = enhancem$rad(WALK_LL_REST[2]);
			}
			if (this.enhancem$isUsingShield(renderState)) {
				this.enhancem$applyShieldGuard(model, renderState);
			}
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
	private boolean enhancem$isUsingShield(ZombieRenderState state) {
		// UndeadRenderState.getUseItemStackForArm always returns the main-hand item.
		return state.isUsingItem && state.useItemHand == InteractionHand.OFF_HAND
			&& (state.mainArm == HumanoidArm.RIGHT ? state.leftHandItemStack : state.rightHandItemStack).is(Items.SHIELD);
	}

	@Unique
	private void enhancem$applyShieldGuard(HumanoidModel<ZombieRenderState> model, ZombieRenderState state) {
		float side = state.mainArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		ModelPart shieldArm = model.getArm(state.mainArm.getOpposite());
		// shield_blocking is level at 90 degrees; rotating past it lowers the outer edge.
		shieldArm.xRot = enhancem$rad(-45.0F);
		shieldArm.yRot = side * enhancem$rad(25.0F);
		shieldArm.zRot = side * enhancem$rad(120.0F);
		// Move the guard toward the shield side and compensate for the grip rising with the roll.
		shieldArm.x = shieldArm.getInitialPose().x() + side * 6.0F * state.ageScale;
		shieldArm.y = shieldArm.getInitialPose().y() + 7.0F * state.ageScale;
		shieldArm.z = shieldArm.getInitialPose().z() - 2.0F * state.ageScale;
	}

	@Unique
	private void enhancem$applyGuardedThrust(HumanoidModel<ZombieRenderState> model, ZombieRenderState state, float progress) {
		boolean rightHanded = state.mainArm == HumanoidArm.RIGHT;
		float side = rightHanded ? 1.0F : -1.0F;
		ModelPart swordArm = rightHanded ? model.rightArm : model.leftArm;
		ModelPart frontLeg = rightHanded ? model.leftLeg : model.rightLeg;
		ModelPart backLeg = rightHanded ? model.rightLeg : model.leftLeg;

		// Coil briefly, thrust to full extension at the server's damage tick, then recover.
		float extension;
		if (progress < 0.2F) {
			extension = 0.0F;
		} else if (progress < (float)SwordZombieAttackAnimation.GUARDED_THRUST_DAMAGE_TICK / SwordZombieAttackAnimation.GUARDED_THRUST_DURATION_TICKS) {
			float hitProgress = (float)SwordZombieAttackAnimation.GUARDED_THRUST_DAMAGE_TICK / SwordZombieAttackAnimation.GUARDED_THRUST_DURATION_TICKS;
			extension = (progress - 0.2F) / (hitProgress - 0.2F);
		} else if (progress < 0.55F) {
			extension = 1.0F;
		} else {
			extension = 1.0F - (progress - 0.55F) / 0.45F;
		}
		extension = (float)Mth.smoothstep(Math.clamp(extension, 0.0F, 1.0F));
		model.body.xRot = enhancem$rad(Mth.lerp(extension, 5.0F, 13.0F));
		model.body.yRot = side * enhancem$rad(Mth.lerp(extension, -22.0F, -8.0F));
		model.body.z = -extension * 1.5F * state.ageScale;

		this.enhancem$applyShieldGuard(model, state);

		// Reset vanilla swing offsets so thrust motion follows only the synced phase.
		swordArm.x = swordArm.getInitialPose().x();
		swordArm.y = swordArm.getInitialPose().y();
		swordArm.xRot = enhancem$rad(Mth.lerp(extension, -65.0F, -90.0F));
		swordArm.yRot = side * enhancem$rad(Mth.lerp(extension, -18.0F, 0.0F));
		swordArm.zRot = side * enhancem$rad(Mth.lerp(extension, -8.0F, 0.0F));
		swordArm.z = Mth.lerp(extension, 1.5F, -4.0F) * state.ageScale;
		frontLeg.xRot = enhancem$rad(Mth.lerp(extension, -12.0F, -25.0F));
		backLeg.xRot = enhancem$rad(Mth.lerp(extension, 10.0F, 18.0F));
		frontLeg.yRot = 0.0F;
		backLeg.yRot = 0.0F;
		frontLeg.zRot = side * enhancem$rad(-4.0F);
		backLeg.zRot = side * enhancem$rad(4.0F);
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

	@Unique
	private static final float[] WALK_RA_REST = {-1.6575F, -12.3914F, 7.6799F};
	@Unique
	private static final float[] WALK_LA_REST = {-4.9811F, -0.4352F, -4.9811F};
	@Unique
	private static final float[] WALK_RL_REST = {0.0F, 0.0F, 7.5F};
	@Unique
	private static final float[] WALK_LL_REST = {0.0F, 0.0F, -5.0F};

	@Unique
	private void enhancem$applySwordWalkAnimation(HumanoidModel<ZombieRenderState> model, ZombieRenderState renderState) {
		float walkTime = renderState.walkAnimationPos;
		float speed = Math.min(renderState.walkAnimationSpeed, 1.0F);

		// 获取行走动画的周期进度 (0.0 到 1.0 循环)
		float cycleProgress = (walkTime * 0.6662F) % Mth.TWO_PI / Mth.TWO_PI;

		// 根据周期进度插值四个关键帧
		float[] raRot, laRot, rlRot, llRot;
		float bodyXRot;

		if (cycleProgress < 0.25F) {
			// 第一关键帧到第二关键帧 (0% -> 25%)
			float t = cycleProgress / 0.25F;
			raRot = this.enhancem$lerpRot(WALK_RA_REST, new float[]{10.8425F, -12.3914F, 7.6799F}, t);
			laRot = this.enhancem$lerpRot(WALK_LA_REST, new float[]{-42.4811F, -0.4352F, -4.9811F}, t);
			rlRot = this.enhancem$lerpRot(WALK_RL_REST, new float[]{-45.0F, 0.0F, 7.5F}, t);
			llRot = this.enhancem$lerpRot(WALK_LL_REST, new float[]{7.5F, 0.0F, -5.0F}, t);
			bodyXRot = Mth.lerp(t, 7.5F, 10.0F);
		} else if (cycleProgress < 0.5F) {
			// 第二关键帧到第三关键帧 (25% -> 50%)
			float t = (cycleProgress - 0.25F) / 0.25F;
			raRot = this.enhancem$lerpRot(new float[]{10.8425F, -12.3914F, 7.6799F}, new float[]{2.5189F, -0.4352F, -4.9811F}, t);
			laRot = this.enhancem$lerpRot(new float[]{-42.4811F, -0.4352F, -4.9811F}, new float[]{2.5189F, -0.4352F, -4.9811F}, t);
			rlRot = this.enhancem$lerpRot(new float[]{-45.0F, 0.0F, 7.5F}, new float[]{5.0F, 0.0F, 7.5F}, t);
			llRot = this.enhancem$lerpRot(new float[]{7.5F, 0.0F, -5.0F}, new float[]{7.5F, 0.0F, -5.0F}, t);
			bodyXRot = Mth.lerp(t, 10.0F, 7.5F);
		} else if (cycleProgress < 0.75F) {
			// 第三关键帧到第四关键帧 (50% -> 75%)
			float t = (cycleProgress - 0.5F) / 0.25F;
			raRot = this.enhancem$lerpRot(new float[]{2.5189F, -0.4352F, -4.9811F}, new float[]{-36.6575F, -12.3914F, 7.6799F}, t);
			laRot = this.enhancem$lerpRot(new float[]{2.5189F, -0.4352F, -4.9811F}, WALK_LA_REST, t);
			rlRot = this.enhancem$lerpRot(new float[]{5.0F, 0.0F, 7.5F}, new float[]{12.5F, 0.0F, 7.5F}, t);
			llRot = this.enhancem$lerpRot(new float[]{7.5F, 0.0F, -5.0F}, new float[]{-47.4753F, -1.3429F, -7.1089F}, t);
			bodyXRot = Mth.lerp(t, 7.5F, 10.0F);
		} else {
			// 第四关键帧回到第一关键帧 (75% -> 100%)
			float t = (cycleProgress - 0.75F) / 0.25F;
			raRot = this.enhancem$lerpRot(new float[]{-36.6575F, -12.3914F, 7.6799F}, WALK_RA_REST, t);
			laRot = this.enhancem$lerpRot(WALK_LA_REST, WALK_LA_REST, t);
			rlRot = this.enhancem$lerpRot(new float[]{12.5F, 0.0F, 7.5F}, WALK_RL_REST, t);
			llRot = this.enhancem$lerpRot(new float[]{-47.4753F, -1.3429F, -7.1089F}, WALK_LL_REST, t);
			bodyXRot = Mth.lerp(t, 10.0F, 7.5F);
		}

		// 应用到模型，使用 speed 作为混合因子
		model.body.xRot = Mth.lerp(speed, model.body.xRot, enhancem$rad(bodyXRot));
		model.body.yRot = 0.0F;
		model.body.z = 0.0F;

		model.rightArm.xRot = Mth.lerp(speed, model.rightArm.xRot, enhancem$rad(raRot[0]));
		model.rightArm.yRot = Mth.lerp(speed, model.rightArm.yRot, enhancem$rad(raRot[1]));
		model.rightArm.zRot = Mth.lerp(speed, model.rightArm.zRot, enhancem$rad(raRot[2]));

		model.leftArm.xRot = Mth.lerp(speed, model.leftArm.xRot, enhancem$rad(laRot[0]));
		model.leftArm.yRot = Mth.lerp(speed, model.leftArm.yRot, enhancem$rad(laRot[1]));
		model.leftArm.zRot = Mth.lerp(speed, model.leftArm.zRot, enhancem$rad(laRot[2]));
		model.leftArm.z = 0.0F;

		model.rightLeg.xRot = Mth.lerp(speed, model.rightLeg.xRot, enhancem$rad(rlRot[0]));
		model.rightLeg.yRot = Mth.lerp(speed, model.rightLeg.yRot, enhancem$rad(rlRot[1]));
		model.rightLeg.zRot = Mth.lerp(speed, model.rightLeg.zRot, enhancem$rad(rlRot[2]));

		model.leftLeg.xRot = Mth.lerp(speed, model.leftLeg.xRot, enhancem$rad(llRot[0]));
		model.leftLeg.yRot = Mth.lerp(speed, model.leftLeg.yRot, enhancem$rad(llRot[1]));
		model.leftLeg.zRot = Mth.lerp(speed, model.leftLeg.zRot, enhancem$rad(llRot[2]));
	}

	@Unique
	private float[] enhancem$lerpRot(float[] from, float[] to, float t) {
		return new float[]{
			Mth.lerp(t, from[0], to[0]),
			Mth.lerp(t, from[1], to[1]),
			Mth.lerp(t, from[2], to[2])
		};
	}
}
