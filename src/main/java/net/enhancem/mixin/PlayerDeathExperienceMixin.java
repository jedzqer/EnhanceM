package net.enhancem.mixin;

import net.enhancem.util.ExperienceDropContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class PlayerDeathExperienceMixin {

	@Inject(method = "dropExperience", at = @At("HEAD"))
	private void enhancem$beginPlayerDeathExperience(ServerLevel level, Entity attacker, CallbackInfo ci) {
		if ((Object) this instanceof Player) {
			ExperienceDropContext.beginPlayerDeathXp();
		}
	}

	@Inject(method = "dropExperience", at = @At("RETURN"))
	private void enhancem$endPlayerDeathExperience(ServerLevel level, Entity attacker, CallbackInfo ci) {
		if ((Object) this instanceof Player) {
			ExperienceDropContext.endPlayerDeathXp();
		}
	}
}
