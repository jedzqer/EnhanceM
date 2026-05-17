package net.enhancem.client;

import net.enhancem.network.RebirthPearlChannelPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EnhanceMClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RebirthPearlParticleHandler.register();

        ClientPlayNetworking.registerGlobalReceiver(RebirthPearlChannelPayload.TYPE, (payload, context) ->
                context.client().execute(() ->
                        RebirthPearlParticleHandler.setChanneling(payload.channeling())
                )
        );
    }
}
