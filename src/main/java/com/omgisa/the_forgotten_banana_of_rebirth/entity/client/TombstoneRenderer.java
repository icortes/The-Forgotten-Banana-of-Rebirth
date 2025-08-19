package com.omgisa.the_forgotten_banana_of_rebirth.entity.client;

import com.omgisa.the_forgotten_banana_of_rebirth.entity.custom.TombstoneEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.NotNull;

public class TombstoneRenderer extends EntityRenderer<TombstoneEntity, EntityRenderState> {
    public TombstoneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(@NotNull TombstoneEntity livingEntity, @NotNull Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public @NotNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
