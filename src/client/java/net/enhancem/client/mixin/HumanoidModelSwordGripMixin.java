package net.enhancem.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.enhancem.SwordZombieAttackAnimation;
import net.enhancem.client.SwordZombieRenderStateAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelSwordGripMixin {
	@Inject(method = "translateToHand", at = @At("TAIL"))
	private void enhancem$pointSwordForward(HumanoidRenderState state, HumanoidArm arm, PoseStack poseStack, CallbackInfo ci) {
		if (state instanceof SwordZombieRenderStateAccess swordState
			&& swordState.enhancem$getSwordAttackPhase() == SwordZombieAttackAnimation.PHASE_GUARDED_THRUST
			&& arm == state.mainArm && state.getMainHandItemStack().is(ItemTags.SWORDS)
			&& state.isUsingItem && state.useItemHand == InteractionHand.OFF_HAND
			&& (arm == HumanoidArm.RIGHT ? state.leftHandItemStack : state.rightHandItemStack).is(Items.SHIELD)) {
			// Rotate at the grip, before ItemInHandLayer's transforms. A raised arm alone
			// leaves the handheld sword pointing up instead of along the thrust.
			float gripX = state.isBaby ? 0.0F : (arm == HumanoidArm.RIGHT ? -1.0F : 1.0F) / 16.0F;
			float gripY = (state.isBaby ? 4.5F : 10.0F) / 16.0F;
			float gripZ = (state.isBaby ? -1.0F : -2.0F) / 16.0F;
			poseStack.rotateAround(Axis.XP.rotationDegrees(80.0F),
				gripX, gripY, gripZ);
		}
	}
}
