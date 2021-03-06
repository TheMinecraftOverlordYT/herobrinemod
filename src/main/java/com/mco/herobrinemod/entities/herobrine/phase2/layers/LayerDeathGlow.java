package com.mco.herobrinemod.entities.herobrine.phase2.layers;

import com.mco.herobrinemod.entities.herobrine.phase2.EntityHardHerobrine;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.Random;

public class LayerDeathGlow<T extends EntityHardHerobrine> implements LayerRenderer<T>
{
    private int up;
    public void doRenderLayer(EntityHardHerobrine entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (entitylivingbaseIn.getDeathTicks() > 233)
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            RenderHelper.disableStandardItemLighting();
            float f = ((float)entitylivingbaseIn.getDeathTicks() + partialTicks) / 200.0F;
            float f1 = 0.0F;

            if (f > 2.8F)
            {
                f1 = (f - 0.8F) / 0.2F;
            }

            Random random = new Random(432L);
            GlStateManager.disableTexture2D();
            GlStateManager.shadeModel(7425);
            GlStateManager.enableBlend();
            if(entitylivingbaseIn.getDeathTicks() < 350){
                GlStateManager.scale(.07F, .07F, .07F);
                up = 0;
            }
            else if(entitylivingbaseIn.getDeathTicks() > 350 && entitylivingbaseIn.getDeathTicks() < 355){
                up += .005;
                GlStateManager.scale(.07F + up, .07F + up, .07F + up);
            }
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            GlStateManager.enableCull();
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 10.0F, -2.0F);

            for (int i = 0; (float)i < (f + f * f) / 2.0F * 60.0F; ++i)
            {
                GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(random.nextFloat() * 360.0F + f * 90.0F, 0.0F, 0.0F, 1.0F);
                float f2 = random.nextFloat() * 20.0F + 5.0F + f1 * 10.0F;
                float f3 = random.nextFloat() * 2.0F + 1.0F + f1 * 2.0F;
                bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos(0.0D, 0.0D, 0.0D).color(255, 255, 255, (int)(255.0F * (1.0F - f1))).endVertex();
                bufferbuilder.pos(-0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(255, 255, 255, 0).endVertex();
                bufferbuilder.pos(0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(255, 255, 255, 0).endVertex();
                bufferbuilder.pos(0.0D, (double)f2, (double)(1.0F * f3)).color(255, 255, 255, 0).endVertex();
                bufferbuilder.pos(-0.866D * (double)f3, (double)f2, (double)(-0.5F * f3)).color(255, 255, 255, 0).endVertex();
                tessellator.draw();
            }

            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            GlStateManager.shadeModel(7424);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
            RenderHelper.enableStandardItemLighting();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
