package com.mrcrayfish.vehicle.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrcrayfish.vehicle.block.RotatedObjectBlock;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.tileentity.GasPumpTankTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.ForgeHooksClient;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankRenderer extends TileEntityRenderer<GasPumpTankTileEntity>
{
    public GasPumpTankRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(GasPumpTankTileEntity gasPump, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, int overlay)
    {
        BlockState state = gasPump.getLevel().getBlockState(gasPump.getBlockPos());
        if(state.getBlock() != ModBlocks.GAS_PUMP.get())
            return;

        matrixStack.pushPose();

        Direction facing = state.getValue(RotatedObjectBlock.DIRECTION);
        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.mulPose(Axis.POSITIVE_Y.rotationDegrees(facing.get2DDataValue() * -90F - 90F));
        matrixStack.translate(-0.5, -0.5, -0.5);
        float height = 11.0F * (gasPump.getFluidTank().getFluidAmount() / (float) gasPump.getFluidTank().getCapacity());
        if(height > 0) drawFluid(gasPump, matrixStack, renderTypeBuffer, 3.01F * 0.0625F, 3F * 0.0625F, 4F * 0.0625F, (10 - 0.02F) * 0.0625F, height * 0.0625F, (8 - 0.02F) * 0.0625F, light);

        matrixStack.popPose();
    }

    private void drawFluid(GasPumpTankTileEntity te, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float x, float y, float z, float width, float height, float depth, int light)
    {
        if(te.getFluidTank().isEmpty())
            return;

        TextureAtlasSprite sprite = ForgeHooksClient.getFluidSprites(te.getLevel(), te.getBlockPos(), te.getFluidTank().getFluid().getFluid().defaultFluidState())[0];
        float minU = sprite.getU0();
        float maxU = Math.min(minU + (sprite.getU1() - minU) * depth, sprite.getU1());
        float minV = sprite.getV0();
        float maxV = Math.min(minV + (sprite.getV1() - minV) * height, sprite.getV1());

        IVertexBuilder buffer = renderTypeBuffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = matrixStack.last().pose();

        //back side
        buffer.vertex(matrix, x + width, y, z + depth).color(0.85F, 0.85F, 0.85F, 1.0F).uv(maxU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x + width, y, z).color(0.85F, 0.85F, 0.85F, 1.0F).uv(minU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x + width, y + height, z).color(0.85F, 0.85F, 0.85F, 1.0F).uv(minU, maxV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x + width, y + height, z + depth).color(0.85F, 0.85F, 0.85F, 1.0F).uv(maxU, maxV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();

        //front side
        buffer.vertex(matrix, x, y, z).color(0.85F, 0.85F, 0.85F, 1.0F).uv(minU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x, y, z + depth).color(0.85F, 0.85F, 0.85F, 1.0F).uv(maxU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x, y + height, z + depth).color(0.85F, 0.85F, 0.85F, 1.0F).uv(maxU, maxV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x, y + height, z).color(0.85F, 0.85F, 0.85F, 1.0F).uv(minU, maxV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();

        maxV = Math.min(minV + (sprite.getV1() - minV) * width, sprite.getV1());

        //top
        buffer.vertex(matrix, x, y + height, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x, y + height, z + depth).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, minV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x + width, y + height, z + depth).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, maxV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.vertex(matrix, x + width, y + height, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, maxV).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
    }
}
