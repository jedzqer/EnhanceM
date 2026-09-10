package net.enhancem.client;

import net.enhancem.EnhanceM;
import net.enhancem.client.renderer.EnderSoldierRenderer;
import net.enhancem.network.RebirthPearlChannelPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class EnhanceMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RebirthPearlParticleHandler.register();
        EntityRenderers.register(EnhanceM.ENDER_SOLDIER, EnderSoldierRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(RebirthPearlChannelPayload.TYPE, (payload, context) ->
                context.client().execute(() ->
                        RebirthPearlParticleHandler.setChanneling(payload.channeling())
                )
        );
    }
}
