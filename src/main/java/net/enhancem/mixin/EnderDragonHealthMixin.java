package net.enhancem.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public class EnderDragonHealthMixin {

	@Unique
	private static final double ENHANCEM_HEALTH_MULTIPLIER = 2.0D;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void enhancem$doubleEnderDragonHealth(EntityType<?> entityType, Level level, CallbackInfo ci) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		AttributeInstance healthAttr = dragon.getAttribute(Attributes.MAX_HEALTH);
		if (healthAttr != null) {
			healthAttr.setBaseValue(healthAttr.getBaseValue() * ENHANCEM_HEALTH_MULTIPLIER);
			dragon.setHealth(dragon.getMaxHealth());
		}
	}
}
