package ir.cairo.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

public class CosmeticsRender {
    final Minecraft mc = Minecraft.getMinecraft();
    static Field capeField;
    static boolean fieldTried;
    Object lastInfo;
    boolean modified;
    ResourceLocation orig;
    final Map<String, ResourceLocation> capeTex = new HashMap<String, ResourceLocation>();
    double petX, petY, petZ, prevX, prevY, prevZ;
    float petYaw, prevYaw;
    boolean petInit;
    int ticks;

    static Field capeField() {
        if (!fieldTried) {
            fieldTried = true;
            try {
                capeField = ReflectionHelper.findField(NetworkPlayerInfo.class, "locationCape", "field_178862_f");
            } catch (Exception e) {
                try {
                    int n = 0;
                    for (Field f : NetworkPlayerInfo.class.getDeclaredFields()) {
                        if (f.getType() == ResourceLocation.class && ++n == 2) { f.setAccessible(true); capeField = f; break; }
                    }
                } catch (Exception ignored) {}
            }
        }
        return capeField;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null) { petInit = false; return; }
        ticks++;
        applyCape(p);
        updatePet(p);
    }

    void applyCape(EntityPlayerSP p) {
        NetHandlerPlayClient nh = mc.getNetHandler();
        if (nh == null) return;
        NetworkPlayerInfo info = nh.getPlayerInfo(p.getUniqueID());
        Field f = capeField();
        if (info == null || f == null) return;
        try {
            if (info != lastInfo) { lastInfo = info; orig = (ResourceLocation) f.get(info); modified = false; }
            String id = Cosmetics.effective('c');
            if (id != null) {
                ResourceLocation r = capeTex.get(id);
                if (r == null) { r = new ResourceLocation("cairoclient", "textures/capes/" + id + ".png"); capeTex.put(id, r); }
                if (f.get(info) != r) f.set(info, r);
                modified = true;
            } else if (modified) {
                f.set(info, orig);
                modified = false;
            }
        } catch (Exception ignored) {}
    }

    void updatePet(EntityPlayerSP p) {
        prevX = petX; prevY = petY; prevZ = petZ; prevYaw = petYaw;
        double yaw = Math.toRadians(p.rotationYaw);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw), rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double tx = p.posX + rx * 0.95 - fx * 0.35, ty = p.posY + 0.45, tz = p.posZ + rz * 0.95 - fz * 0.35;
        if (!petInit || Math.abs(tx - petX) > 10 || Math.abs(tz - petZ) > 10 || Math.abs(ty - petY) > 10) {
            petX = tx; petY = ty; petZ = tz; petYaw = p.rotationYaw;
            prevX = petX; prevY = petY; prevZ = petZ; prevYaw = petYaw;
            petInit = true;
        }
        petX += (tx - petX) * 0.18;
        petY += (ty - petY) * 0.18;
        petZ += (tz - petZ) * 0.18;
        petYaw += MathHelper.wrapAngleTo180_float(p.rotationYaw - petYaw) * 0.2f;
    }

    // ---------------- drawing helpers ----------------
    static void v(WorldRenderer w, double x, double y, double z, int c) {
        w.pos(x, y, z).color((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255).endVertex();
    }

    static void q(WorldRenderer w, int c, float sh, double... p) {
        int r = (int) (((c >> 16) & 255) * sh), g = (int) (((c >> 8) & 255) * sh), b = (int) ((c & 255) * sh), a = (c >>> 24) & 255;
        for (int i = 0; i < 4; i++) w.pos(p[i * 3], p[i * 3 + 1], p[i * 3 + 2]).color(r, g, b, a).endVertex();
    }

    static void box(double x1, double y1, double z1, double x2, double y2, double z2, int c) {
        WorldRenderer w = Tessellator.getInstance().getWorldRenderer();
        w.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        q(w, c, 1.0f, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1);
        q(w, c, 0.5f, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2);
        q(w, c, 0.85f, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2);
        q(w, c, 0.65f, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1);
        q(w, c, 0.75f, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1);
        q(w, c, 0.75f, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
        Tessellator.getInstance().draw();
    }

    static void feather(WorldRenderer w, double ang, double len, double wd, int c1, int c2) {
        double a = Math.toRadians(ang), dx = Math.cos(a), dy = Math.sin(a), px = -dy, py = dx;
        double bx = dx * 0.12, by = dy * 0.12;
        v(w, bx + px * wd / 2, by + py * wd / 2, 0, c1);
        v(w, bx - px * wd / 2, by - py * wd / 2, 0, c1);
        v(w, dx * len, dy * len, 0, c2);
    }

    static void feathered(int n, double a0, double a1, double l0, double l1, double wd, int c1, int c2) {
        WorldRenderer w = Tessellator.getInstance().getWorldRenderer();
        w.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < n; i++) {
            double t = n == 1 ? 0 : (double) i / (n - 1);
            feather(w, a0 + (a1 - a0) * t, l0 + (l1 - l0) * t, wd, c1, c2);
        }
        Tessellator.getInstance().draw();
    }

    static void bone(WorldRenderer w, double x1, double y1, double x2, double y2, double wd, int c) {
        double dx = x2 - x1, dy = y2 - y1, l = Math.sqrt(dx * dx + dy * dy);
        if (l == 0) return;
        double px = -dy / l * wd / 2, py = dx / l * wd / 2;
        v(w, x1 + px, y1 + py, 0, c); v(w, x1 - px, y1 - py, 0, c); v(w, x2 + px, y2 + py, 0, c);
        v(w, x1 - px, y1 - py, 0, c); v(w, x2 - px, y2 - py, 0, c); v(w, x2 + px, y2 + py, 0, c);
    }

    static void tri(WorldRenderer w, double x1, double y1, int c1, double x2, double y2, int c2, double x3, double y3, int c3) {
        v(w, x1, y1, 0, c1); v(w, x2, y2, 0, c2); v(w, x3, y3, 0, c3);
    }

    static void shape(String id) {
        if (id.equals("w1")) {
            feathered(6, 70, -15, 1.0, 0.55, 0.30, 0xDDFFF3C4, 0x66FFD54F);
            feathered(7, 78, -25, 1.35, 0.7, 0.30, 0xF5FFFFFF, 0x99CFE8FF);
        } else if (id.equals("w3")) {
            feathered(6, 70, -15, 1.0, 0.55, 0.30, 0xDDFF8F00, 0x66D50000);
            feathered(7, 78, -25, 1.4, 0.7, 0.30, 0xF5FFD54F, 0xAAFF1744);
        } else if (id.equals("w4")) {
            feathered(5, 62, -26, 1.55, 0.55, 0.16, 0xF000E5FF, 0xB0FF00AA);
            feathered(5, 62, -26, 1.55, 0.55, 0.04, 0xFFFFFFFF, 0xFFFFFFFF);
        } else if (id.equals("w2")) {
            double[][] t = {{1.95, 0.75}, {1.7, 0.05}, {1.2, -0.55}, {0.55, -0.75}};
            double ex = 0.45, ey = 0.95, wx = 1.35, wy = 1.35;
            int root = 0xF0C62828, edge = 0xF03B0D0D, bn = 0xFF303030;
            WorldRenderer w = Tessellator.getInstance().getWorldRenderer();
            w.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            tri(w, 0, 0, root, wx, wy, edge, t[0][0], t[0][1], edge);
            for (int i = 0; i < 3; i++) tri(w, 0, 0, root, t[i][0], t[i][1], edge, t[i + 1][0], t[i + 1][1], edge);
            bone(w, 0, 0, ex, ey, 0.08, bn);
            bone(w, ex, ey, wx, wy, 0.07, bn);
            for (int i = 0; i < 4; i++) bone(w, wx, wy, t[i][0], t[i][1], 0.05, bn);
            Tessellator.getInstance().draw();
        }
    }

    void drawWings(String id, EntityPlayer p, double x, double y, double z, float pt) {
        float yaw = p.prevRenderYawOffset + (p.renderYawOffset - p.prevRenderYawOffset) * pt;
        boolean sn = p.isSneaking();
        float t = (System.currentTimeMillis() % 100000L) / 1000f;
        float flap = (float) Math.sin(t * (p.onGround ? 2.2 : 6.0)) * (p.onGround ? 7 : 16);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-yaw, 0, 1, 0);
        GlStateManager.translate(0, sn ? 1.12 : 1.38, sn ? -0.3 : -0.16);
        if (sn) GlStateManager.rotate(28, 1, 0, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        for (int side = 0; side < 2; side++) {
            GlStateManager.pushMatrix();
            if (side == 1) GlStateManager.scale(-1f, 1f, 1f);
            GlStateManager.rotate(34 + flap, 0, 1, 0);
            shape(id);
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void wings(RenderPlayerEvent.Post e) {
        if (e.entityPlayer != mc.thePlayer || e.entityPlayer.isInvisible()) return;
        String id = Cosmetics.effective('w');
        if (id == null) return;
        drawWings(id, e.entityPlayer, e.x, e.y, e.z, e.partialRenderTick);
    }

    // ---------------- pets ----------------
    void drawPet(String id, float t) {
        if (id.equals("p1")) {
            box(-0.07, 0.09, -0.07, 0.07, 0.23, 0.07, 0xFFB71C1C);
            box(-0.16, 0.0, -0.16, 0.16, 0.32, 0.16, 0xB0E53935);
            box(-0.1, 0.18, 0.162, -0.04, 0.25, 0.175, 0xFF111111);
            box(0.04, 0.18, 0.162, 0.1, 0.25, 0.175, 0xFF111111);
        } else if (id.equals("p2")) {
            int o = 0xFFE8751A, d = 0xFF3E2723;
            box(-0.11, 0.12, -0.2, 0.11, 0.3, 0.12, o);
            box(-0.1, 0.2, 0.12, 0.1, 0.38, 0.3, o);
            box(-0.05, 0.22, 0.3, 0.05, 0.29, 0.38, 0xFFFFFFFF);
            box(-0.02, 0.27, 0.38, 0.02, 0.3, 0.4, 0xFF111111);
            box(-0.1, 0.38, 0.14, -0.04, 0.46, 0.2, d);
            box(0.04, 0.38, 0.14, 0.1, 0.46, 0.2, d);
            box(-0.07, 0.31, 0.3, -0.04, 0.34, 0.306, 0xFF111111);
            box(0.04, 0.31, 0.3, 0.07, 0.34, 0.306, 0xFF111111);
            box(-0.1, 0, -0.18, -0.04, 0.12, -0.12, d);
            box(0.04, 0, -0.18, 0.1, 0.12, -0.12, d);
            box(-0.1, 0, 0.04, -0.04, 0.12, 0.1, d);
            box(0.04, 0, 0.04, 0.1, 0.12, 0.1, d);
            box(-0.05, 0.15, -0.46, 0.05, 0.27, -0.2, o);
            box(-0.05, 0.15, -0.54, 0.05, 0.27, -0.46, 0xFFFFFFFF);
        } else if (id.equals("p3")) {
            int g = 0xFF616161, r = 0xFFE53935;
            box(-0.1, 0.12, -0.22, 0.1, 0.3, 0.1, g);
            box(-0.09, 0.22, 0.1, 0.09, 0.38, 0.28, r);
            box(-0.05, 0.24, 0.28, 0.05, 0.31, 0.34, 0xFFB71C1C);
            box(-0.08, 0.38, 0.12, -0.05, 0.46, 0.15, 0xFF212121);
            box(0.05, 0.38, 0.12, 0.08, 0.46, 0.15, 0xFF212121);
            box(-0.07, 0.32, 0.28, -0.04, 0.35, 0.286, 0xFFFFEB3B);
            box(0.04, 0.32, 0.28, 0.07, 0.35, 0.286, 0xFFFFEB3B);
            box(-0.04, 0.18, -0.4, 0.04, 0.26, -0.22, g);
            box(-0.02, 0.2, -0.5, 0.02, 0.25, -0.4, r);
            for (int s = -1; s <= 1; s += 2) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(s * 0.1, 0.28, -0.04);
                GlStateManager.scale((float) s, 1f, 1f);
                GlStateManager.rotate(25f + 25f * (float) Math.sin(t * 0.3), 0, 0, 1);
                box(0, 0, -0.1, 0.35, 0.02, 0.12, 0xFFB71C1C);
                GlStateManager.popMatrix();
            }
        }
    }

    @SubscribeEvent
    public void world(RenderWorldLastEvent e) {
        String id = Cosmetics.effective('p');
        Entity view = mc.getRenderViewEntity();
        if (id == null || mc.thePlayer == null || view == null || !petInit) return;
        float pt = e.partialTicks;
        double cx = view.lastTickPosX + (view.posX - view.lastTickPosX) * pt;
        double cy = view.lastTickPosY + (view.posY - view.lastTickPosY) * pt;
        double cz = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * pt;
        double px = prevX + (petX - prevX) * pt, py = prevY + (petY - prevY) * pt, pz = prevZ + (petZ - prevZ) * pt;
        float yaw = prevYaw + (petYaw - prevYaw) * pt;
        float t = ticks + pt;
        double bob = Math.sin(t * 0.12) * 0.06;
        GlStateManager.pushMatrix();
        GlStateManager.translate(px - cx, py - cy + bob, pz - cz);
        GlStateManager.rotate(-yaw, 0, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);
        drawPet(id, t);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
