package net.enhancem.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;

public class RebirthPearlParticleHandler {
    private static boolean channeling = false;
    private static int tickCount = 0;
    private static double angle = 0.0;

    public static void setChanneling(boolean active) {
        channeling = active;
        if (!active) {
            tickCount = 0;
            angle = 0.0;
        }
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(RebirthPearlParticleHandler::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        if (!channeling || client.player == null || client.level == null) return;

        LocalPlayer player = client.player;
        tickCount++;

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        // Three interleaved helices rotating around the player
        for (int strand = 0; strand < 3; strand++) {
            double strandAngle = angle + strand * (Math.PI * 2.0 / 3.0);
            double radius = 1.1 + 0.3 * Math.sin(tickCount * 0.15);

            // Height sweeps from 0 to 2 over 40 ticks, then resets
            double height = ((tickCount % 40) / 40.0) * 2.0;

            double x = px + Math.cos(strandAngle) * radius;
            double z = pz + Math.sin(strandAngle) * radius;
            double y = py + height;

            // Velocity: slight inward pull + upward drift
            double vx = (px - x) * 0.04;
            double vy = 0.04;
            double vz = (pz - z) * 0.04;

            client.level.addParticle(ParticleTypes.PORTAL, x, y, z, vx, vy, vz);
        }

        // Advance angle — full rotation every ~21 ticks
        angle += 0.3;
    }
}
