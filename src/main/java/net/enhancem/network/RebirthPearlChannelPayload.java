package net.enhancem.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RebirthPearlChannelPayload(boolean channeling) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RebirthPearlChannelPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("enhancem", "rebirth_pearl_channel"));
    public static final StreamCodec<ByteBuf, RebirthPearlChannelPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, RebirthPearlChannelPayload::channeling, RebirthPearlChannelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
