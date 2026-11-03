package ir.cairo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.opengl.Display;

public class FpsBoost {
    public static void apply() {
        Minecraft mc = Minecraft.getMinecraft();
        GameSettings gs = mc.gameSettings;
        gs.fancyGraphics = false;
        gs.clouds = 0;
        gs.particleSetting = 2;
        gs.ambientOcclusion = 0;
        gs.entityShadows = false;
        gs.enableVsync = false;
        gs.limitFramerate = 260;
        gs.mipmapLevels = 0;
        gs.fboEnable = true;
        if (gs.renderDistanceChunks > 10) gs.renderDistanceChunks = 10;
        gs.saveOptions();
        Display.setVSyncEnabled(false);
    }
}
