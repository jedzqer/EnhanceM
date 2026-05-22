package net.enhancem.client.renderer;

import net.enhancem.EnhanceM;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public class EnderSoldierRenderer extends ZombieRenderer {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(EnhanceM.MOD_ID, "textures/entity/ender_soldier.png");

    public EnderSoldierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState renderState) {
        return TEXTURE;
    }
}
