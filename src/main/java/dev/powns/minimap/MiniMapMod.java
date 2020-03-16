package dev.powns.minimap;

import dev.powns.minimap.gui.MiniMapRenderer;
import dev.powns.minimap.listener.MiniMapListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid="pownsminimap", name="MiniMap Mod", version="1.0")
public class MiniMapMod
{
    private static MiniMapMod instance;
    private MiniMapRenderer miniMapRenderer;

    public MiniMapMod(){
        miniMapRenderer = new MiniMapRenderer();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        instance = this;

        FMLCommonHandler.instance().bus().register(new MiniMapListener());
    }

    public static MiniMapMod getInstance() {
        return instance;
    }

    public MiniMapRenderer getMiniMapRenderer() {
        return miniMapRenderer;
    }
}
