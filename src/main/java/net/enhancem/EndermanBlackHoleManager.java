package net.enhancem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class EndermanBlackHoleManager {

    private static final double EFFECT_RADIUS = 10.0D;
    private static final double CORE_RADIUS = 1.5D;
    private static final double MAX_PULL_SPEED = 0.55D;
    private static final double DAMAGE_PER_TICK = 2.0D;
    private static final int DURATION_TICKS = 80;
    private static final float TELEPORT_CHANCE = 0.35F;

    private static final List<BlackHole> ACTIVE = new ArrayList<>();

    private EndermanBlackHoleManager() {
    }

    public static void spawn(ServerLevel level, EnderMan enderman) {
        ACTIVE.add(new BlackHole(level, enderman.position(), level.getGameTime() + DURATION_TICKS));
    }

    public static void tick(ServerLevel level) {
        if (ACTIVE.isEmpty()) {
            return;
        }

        Iterator<BlackHole> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            BlackHole blackHole = iterator.next();
            if (blackHole.level != level || level.getGameTime() >= blackHole.expiresAt) {
                iterator.remove();
                continue;
            }

            AABB area = AABB.ofSize(blackHole.center, EFFECT_RADIUS * 2.0D, EFFECT_RADIUS * 2.0D, EFFECT_RADIUS * 2.0D);
            for (Entity entity : level.getEntitiesOfClass(Entity.class, area, entity -> entity.isAlive() && entity.distanceToSqr(blackHole.center) <= EFFECT_RADIUS * EFFECT_RADIUS)) {
                applyPull(level, blackHole.center, entity);
            }
        }
    }

    private static void applyPull(ServerLevel level, Vec3 center, Entity entity) {
        Vec3 delta = center.subtract(entity.position());
        double distance = delta.length();
        if (distance < 0.001D) {
            distance = 0.001D;
        }

        double strength = Math.min(MAX_PULL_SPEED, (EFFECT_RADIUS - distance) / EFFECT_RADIUS * MAX_PULL_SPEED);
        Vec3 motion = delta.scale(strength / distance);
        entity.setDeltaMovement(entity.getDeltaMovement().add(motion));

        if (entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().magic(), (float) DAMAGE_PER_TICK);
        }

        if (entity instanceof Player player && distance <= CORE_RADIUS) {
            if (player.getRandom().nextFloat() < TELEPORT_CHANCE) {
                teleportPlayer(player);
            }
        }

        if (entity instanceof ItemEntity itemEntity && distance <= CORE_RADIUS) {
            itemEntity.setPickUpDelay(20);
        }
    }

    private static void teleportPlayer(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        for (int i = 0; i < 16; i++) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 32.0D;
            double y = player.getY() + player.getRandom().nextInt(17) - 8;
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 32.0D;
            if (player.randomTeleport(x, y, z, true)) {
                return;
            }
        }
    }

    private record BlackHole(ServerLevel level, Vec3 center, long expiresAt) {
    }
}
