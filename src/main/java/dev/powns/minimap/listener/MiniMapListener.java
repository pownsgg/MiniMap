package dev.powns.minimap.listener;

import dev.powns.minimap.MiniMapMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MiniMapListener {

    private Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld != null && mc.thePlayer != null) {
            MiniMapMod.getInstance().getMiniMapRenderer().updateMapData(mc.theWorld, mc.thePlayer);
            MiniMapMod.getInstance().getMiniMapRenderer().updateMapTexture();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        if (mc.currentScreen == null || mc.ingameGUI.getChatGUI().getChatOpen()) {
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int screenWidth = scaledResolution.getScaledWidth(), hudSpacing = 5, miniMapSize = 45;

            MiniMapMod.getInstance().getMiniMapRenderer().renderMiniMap(screenWidth - hudSpacing - miniMapSize, hudSpacing + miniMapSize, miniMapSize);
        }
    }


}