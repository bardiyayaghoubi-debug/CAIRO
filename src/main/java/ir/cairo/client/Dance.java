package ir.cairo.client;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** dance emote for the local player (shown in third person / the preview) */
public class Dance {
    public static boolean active = false;
    public static long start = 0;
    static int savedView = 0;
    static DanceModel normal, slimModel;
    static Field modelField;
    static boolean tried;
    Object saved;
    RendererLivingEntity swapped;

    public static void toggle() {
        Minecraft mc = Minecraft.getMinecraft();
        if (active) { stop(); return; }
        active = true;
        start = System.currentTimeMillis();
        savedView = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 2;
    }

    public static void stop() {
        if (!active) return;
        active = false;
        Minecraft.getMinecraft().gameSettings.thirdPersonView = savedView;
    }

    public static void tick(Minecraft mc) {
        if (!active) return;
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) { active = false; return; }
        MovementInput mi = p.movementInput;
        if (mi != null && (Math.abs(mi.moveForward) > 0.01f || Math.abs(mi.moveStrafe) > 0.01f || mi.jump || mi.sneak)) stop();
        if (!Config.get("dance")) stop();
    }

    static Field field() {
        if (!tried) {
            tried = true;
            try { modelField = ReflectionHelper.findField(RendererLivingEntity.class, "mainModel", "field_77045_g"); }
            catch (Exception ignored) {}
        }
        return modelField;
    }

    @SubscribeEvent
    public void pre(RenderPlayerEvent.Pre e) {
        if (!active || e.entityPlayer != Minecraft.getMinecraft().thePlayer) return;
        Field f = field();
        if (f == null) return;
        try {
            if (normal == null) { normal = new DanceModel(0f, false); slimModel = new DanceModel(0f, true); }
            boolean slim = e.entityPlayer instanceof AbstractClientPlayer && "slim".equals(((AbstractClientPlayer) e.entityPlayer).getSkinType());
            saved = f.get(e.renderer);
            f.set(e.renderer, slim ? slimModel : normal);
            swapped = e.renderer;
        } catch (Exception ignored) {
            swapped = null;
        }
    }

    @SubscribeEvent
    public void post(RenderPlayerEvent.Post e) {
        if (swapped == null) return;
        try { field().set(swapped, saved); } catch (Exception ignored) {}
        swapped = null;
        saved = null;
    }
}
