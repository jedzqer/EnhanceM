package net.enhancem.mixin;

import net.enhancem.EndermanBlackHoleManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class EnhanceMMixin {
	@Inject(at = @At("HEAD"), method = "loadLevel")
	private void init(CallbackInfo info) {
	}

	@Inject(method = "tickServer", at = @At("HEAD"))
	private void enhancem$tickBlackHoles(CallbackInfo ci) {
		MinecraftServer server = (MinecraftServer) (Object) this;
		for (ServerLevel level : server.getAllLevels()) {
			EndermanBlackHoleManager.tick(level);
		}
	}
}
