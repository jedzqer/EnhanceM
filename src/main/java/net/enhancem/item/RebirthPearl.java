package net.enhancem.item;

import net.enhancem.network.RebirthPearlChannelPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RebirthPearl extends Item {
    public static final Map<UUID, Long> CHANNELING = new HashMap<>();

    public RebirthPearl(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (CHANNELING.containsKey(player.getUUID())) {
            return InteractionResult.FAIL;
        }

        stack.shrink(1);
        CHANNELING.put(player.getUUID(), level.getGameTime());
        ServerPlayNetworking.send(serverPlayer, new RebirthPearlChannelPayload(true));
        serverPlayer.sendOverlayMessage(
                Component.translatable("item.enhancem.rebirth_pearl.channeling"));

        return InteractionResult.SUCCESS;
    }
}
