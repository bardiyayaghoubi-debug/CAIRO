package ir.cairo.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "cairoclient", name = "Cairo Client", version = "1.0.0", acceptedMinecraftVersions = "[1.8.9]")
public class CairoClient {
    public static KeyBinding menuKey = new KeyBinding("Open Cairo Menu", Keyboard.KEY_RSHIFT, "Cairo Client");

    @Mod.EventHandler
    public void pre(FMLPreInitializationEvent e) {
        Config.load(e.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        ClientRegistry.registerKeyBinding(menuKey);
        Events h = new Events();
        MinecraftForge.EVENT_BUS.register(h);
        FMLCommonHandler.instance().bus().register(h);
        CosmeticsRender cr = new CosmeticsRender();
        MinecraftForge.EVENT_BUS.register(cr);
        FMLCommonHandler.instance().bus().register(cr);
        Account.applySaved();
    }
}
