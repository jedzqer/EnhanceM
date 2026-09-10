package net.enhancem.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class PiglinCrossbowHitEffectMixin {

    @Unique
    private static final double ENHANCEM_CROSSBOW_KNOCKBACK_STRENGTH = 1.5D;
    @Unique
    private static final int ENHANCEM_SLOWNESS_DURATION_TICKS = 20;
    @Unique
    private static final int ENHANCEM_SLOWNESS_AMPLIFIER = 0;

    @Shadow
    public abstract ItemStack getWeaponItem();

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void enhancem$applyPiglinCrossbowHitEffects(EntityHitResult hitResult, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.level().isClientSide()) {
            return;
        }

        Entity ownerEntity = arrow.getOwner();
        if (!(ownerEntity instanceof Piglin owner) || !this.getWeaponItem().is(Items.CROSSBOW)) {
            return;
        }

        if (!(hitResult.getEntity() instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }

        // 26.2: the old knockback(double,double,double) overload is gone; the 5-arg overload is a
        // bridge to knockback(double,double,double,DamageSource,float,boolean). In that method the
        // first double is the knockback strength (scaled by KNOCKBACK_RESISTANCE) and the two
        // following doubles are the horizontal direction; the DamageSource/float/boolean params are
        // never read, so passing the strength first preserves the pre-26.2 behaviour exactly.
        target.knockback(
            ENHANCEM_CROSSBOW_KNOCKBACK_STRENGTH,
            owner.getX() - target.getX(),
            owner.getZ() - target.getZ(),
            arrow.damageSources().arrow(arrow, owner),
            (float) ENHANCEM_CROSSBOW_KNOCKBACK_STRENGTH
        );
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ENHANCEM_SLOWNESS_DURATION_TICKS, ENHANCEM_SLOWNESS_AMPLIFIER), owner);
    }
}
