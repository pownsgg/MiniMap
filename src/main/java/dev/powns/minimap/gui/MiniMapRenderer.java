package dev.powns.minimap.gui;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;

public class MiniMapRenderer {

    private static final ResourceLocation MAP_BORDER = new ResourceLocation("pownsminimap", "border.png");
    private static final ResourceLocation PLAYER_MARKER = new ResourceLocation("pownsminimap", "playermarker.png");

    private byte[] MAP_COLORS = new byte[16384];
    private DynamicTexture MAP_TEXTURE;

    private Minecraft mc = Minecraft.getMinecraft();

    public MiniMapRenderer(){
        MAP_TEXTURE = new DynamicTexture(128, 128);

        for (int i = 0; i < MAP_TEXTURE.getTextureData().length; ++i)
        {
            MAP_TEXTURE.getTextureData()[i] = 0;
        }
    }

    public void renderMiniMap(int x, int y, int radius){
        float rotationYaw = mc.thePlayer.rotationYaw;

        //Resetting the color just in case other mods forget to reset it theirselves...
        //Example: Canelex Keystrokes mod
        GlStateManager.pushMatrix();
        GlStateManager.color(1,1,1,1);

        mc.getTextureManager().bindTexture(MAP_BORDER);
        drawCircle(x, y, radius + 3, 0);

        mc.getTextureManager().bindTexture(mc.getTextureManager().getDynamicTextureLocation("minimap", MAP_TEXTURE ));
        drawCircle(x, y, radius, 180 - rotationYaw);

        mc.getTextureManager().bindTexture(PLAYER_MARKER);
        Gui.drawModalRectWithCustomSizedTexture(x - 4, y - 4, 0, 0, 8, 8, 8, 8);

        drawDirections(x - 2, y - 4, radius, 90 - rotationYaw);

        GlStateManager.popMatrix();
    }

    private void drawDirections(int x, int y, float radius, float rotation){
        String[] directions = new String[]{"N", "E", "S", "W"};

        for(int i = 0; i < 4; i++){
            int xPos = (int) (MathHelper.cos((float) Math.toRadians(rotation + (i * 90))) * radius);
            int yPos = (int) (MathHelper.sin((float) Math.toRadians(rotation + (i * 90))) * radius);

            mc.fontRendererObj.drawStringWithShadow(directions[i], xPos + x, yPos + y, 0xFFFFFFFF);
        }
    }

    private void drawCircle(int x, int y, float radius, float rotation){
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        GlStateManager.translate(x, y, 0);
        GlStateManager.rotate(rotation, 0, 0, 1);

        GL11.glBegin(GL11.GL_POLYGON);
        for(int i = 360; i >= 0; i-=5){
            GL11.glTexCoord2f((MathHelper.cos((float) Math.toRadians(i)) + 1) * 0.5f, (MathHelper.sin((float) Math.toRadians(i)) + 1) * 0.5f);
            GL11.glVertex2f(MathHelper.cos((float) Math.toRadians(i)) * radius, MathHelper.sin((float) Math.toRadians(i)) * radius);
        }

        GL11.glEnd();
        GlStateManager.popMatrix();
    }

    public void updateMapData(World worldIn, Entity viewer) {
        int i = 1;
        int j = mc.thePlayer.getPosition().getX();
        int k = mc.thePlayer.getPosition().getZ();
        int l = MathHelper.floor_double(viewer.posX - (double) j) / i + 64;
        int i1 = MathHelper.floor_double(viewer.posZ - (double) k) / i + 64;
        int j1 = 128 / i;

        if (worldIn.provider.getHasNoSky()) {
            j1 /= 2;
        }

        for (int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
            double d0 = 0.0D;

            for (int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
                if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {
                    int i2 = k1 - l;
                    int j2 = l1 - i1;
                    boolean flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
                    int k2 = (j / i + k1 - 64) * i;
                    int l2 = (k / i + l1 - 64) * i;
                    Multiset<MapColor> multiset = HashMultiset.<MapColor>create();
                    Chunk chunk = worldIn.getChunkFromBlockCoords(new BlockPos(k2, 0, l2));

                    if (!chunk.isEmpty()) {
                        int i3 = k2 & 15;
                        int j3 = l2 & 15;
                        int k3 = 0;
                        double d1 = 0.0D;

                        if (worldIn.provider.getHasNoSky()) {
                            int l3 = k2 + l2 * 231871;
                            l3 = l3 * l3 * 31287121 + l3 * 11;

                            if ((l3 >> 20 & 1) == 0) {
                                multiset.add(Blocks.dirt.getMapColor(Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT)), 10);
                            } else {
                                multiset.add(Blocks.stone.getMapColor(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE)), 100);
                            }

                            d1 = 100.0D;
                        } else {
                            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                            for (int i4 = 0; i4 < i; ++i4) {
                                for (int j4 = 0; j4 < i; ++j4) {
                                    int k4 = chunk.getHeightValue(i4 + i3, j4 + j3) + 1;
                                    IBlockState iblockstate = Blocks.air.getDefaultState();

                                    if (k4 > 1) {
                                        label541:
                                        {
                                            while (true) {
                                                --k4;
                                                iblockstate = chunk.getBlockState(blockpos$mutableblockpos.set(i4 + i3, k4, j4 + j3));

                                                if (iblockstate.getBlock().getMapColor(iblockstate) != MapColor.airColor || k4 <= 0) {
                                                    break;
                                                }
                                            }

                                            if (k4 > 0 && iblockstate.getBlock().getMaterial().isLiquid()) {
                                                int l4 = k4 - 1;

                                                while (true) {
                                                    Block block = chunk.getBlock(i4 + i3, l4--, j4 + j3);
                                                    ++k3;

                                                    if (l4 <= 0 || !block.getMaterial().isLiquid()) {
                                                        break label541;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    d1 += (double) k4 / (double) (i * i);
                                    multiset.add(iblockstate.getBlock().getMapColor(iblockstate));
                                }
                            }
                        }

                        k3 = k3 / (i * i);
                        double d2 = (d1 - d0) * 4.0D / (double) (i + 4) + ((double) (k1 + l1 & 1) - 0.5D) * 0.4D;
                        int i5 = 1;

                        if (d2 > 0.6D) {
                            i5 = 2;
                        }

                        if (d2 < -0.6D) {
                            i5 = 0;
                        }

                        MapColor mapcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.airColor);

                        if (mapcolor == MapColor.waterColor) {
                            d2 = (double) k3 * 0.1D + (double) (k1 + l1 & 1) * 0.2D;
                            i5 = 1;

                            if (d2 < 0.5D) {
                                i5 = 2;
                            }

                            if (d2 > 0.9D) {
                                i5 = 0;
                            }
                        }

                        d0 = d1;

                        if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {
                            byte b0 = MAP_COLORS[k1 + l1 * 128];
                            byte b1 = (byte) (mapcolor.colorIndex * 4 + i5);

                            if (b0 != b1) {
                                MAP_COLORS[k1 + l1 * 128] = b1;
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateMapTexture() {
        for (int i = 0; i < 16384; i++) {
            int j = MAP_COLORS[i] & 255;

            if (j / 4 == 0) {
                MAP_TEXTURE.getTextureData()[i] = (i + i / 128 & 1) * 8 + 16 << 24;
            } else {
                MAP_TEXTURE.getTextureData()[i] = MapColor.mapColorArray[j / 4].func_151643_b(j & 3);
            }
        }

        MAP_TEXTURE.updateDynamicTexture();
    }

}
