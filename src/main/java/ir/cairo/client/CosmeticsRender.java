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
        double tx, ty, tz;
        if (Dance.active) {
            double a = (System.currentTimeMillis() % 100000L) / 400.0;
            tx = p.posX + Math.cos(a) * 1.4;
            tz = p.posZ + Math.sin(a) * 1.4;
            ty = p.posY + 1.2 + Math.sin(a * 2) * 0.25;
        } else {
            double yaw = Math.toRadians(p.rotationYaw);
            double fx = -Math.sin(yaw), fz = Math.cos(yaw), rx = -Math.cos(yaw), rz = -Math.sin(yaw);
            tx = p.posX + rx * 0.95 - fx * 0.35;
            ty = p.posY + 0.9;
            tz = p.posZ + rz * 0.95 - fz * 0.35;
        }
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

    static int lerpC(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int aa = (a >>> 24) & 255, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = (b >>> 24) & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    static double hash(double a, double b) {
        double h = Math.sin(a * 127.1 + b * 311.7) * 43758.5453;
        return h - Math.floor(h);
    }

    static void tri(WorldRenderer w, double x1, double y1, int c1, double x2, double y2, int c2, double x3, double y3, int c3) {
        v(w, x1, y1, 0, c1); v(w, x2, y2, 0, c2); v(w, x3, y3, 0, c3);
    }

    static void bone(WorldRenderer w, double x1, double y1, double x2, double y2, double wd, int c) {
        double dx = x2 - x1, dy = y2 - y1, l = Math.sqrt(dx * dx + dy * dy);
        if (l == 0) return;
        double px = -dy / l * wd / 2, py = dx / l * wd / 2;
        v(w, x1 + px, y1 + py, 0, c); v(w, x1 - px, y1 - py, 0, c); v(w, x2 + px, y2 + py, 0, c);
        v(w, x1 - px, y1 - py, 0, c); v(w, x2 - px, y2 - py, 0, c); v(w, x2 + px, y2 + py, 0, c);
    }

    static double[][] bez(double x0, double y0, double x1, double y1, double x2, double y2, int n) {
        double[][] p = new double[n + 1][2];
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n, u = 1 - t;
            p[i][0] = u * u * x0 + 2 * u * t * x1 + t * t * x2;
            p[i][1] = u * u * y0 + 2 * u * t * y1 + t * t * y2;
        }
        return p;
    }

    /** thick curved strip along a polyline */
    static void strip(WorldRenderer w, double[][] pts, double w0, double w1, int c0, int c1) {
        int n = pts.length;
        for (int i = 0; i < n - 1; i++) {
            double t0 = (double) i / (n - 1), t1 = (double) (i + 1) / (n - 1);
            double wa = w0 + (w1 - w0) * t0, wb = w0 + (w1 - w0) * t1;
            int ca = lerpC(c0, c1, t0), cb = lerpC(c0, c1, t1);
            double dx = pts[i + 1][0] - pts[i][0], dy = pts[i + 1][1] - pts[i][1], l = Math.sqrt(dx * dx + dy * dy);
            if (l == 0) continue;
            double nx = -dy / l, ny = dx / l;
            v(w, pts[i][0] + nx * wa / 2, pts[i][1] + ny * wa / 2, 0, ca);
            v(w, pts[i][0] - nx * wa / 2, pts[i][1] - ny * wa / 2, 0, ca);
            v(w, pts[i + 1][0] + nx * wb / 2, pts[i + 1][1] + ny * wb / 2, 0, cb);
            v(w, pts[i][0] - nx * wa / 2, pts[i][1] - ny * wa / 2, 0, ca);
            v(w, pts[i + 1][0] - nx * wb / 2, pts[i + 1][1] - ny * wb / 2, 0, cb);
            v(w, pts[i + 1][0] + nx * wb / 2, pts[i + 1][1] + ny * wb / 2, 0, cb);
        }
    }

    /** filled (rotated) ellipse, c1 in the middle, c2 at the edge */
    static void fan(WorldRenderer w, double cx, double cy, double rx, double ry, double rotDeg, int c1, int c2, int seg) {
        double rot = Math.toRadians(rotDeg), cr = Math.cos(rot), sr = Math.sin(rot);
        for (int i = 0; i < seg; i++) {
            double a0 = 2 * Math.PI * i / seg, a1 = 2 * Math.PI * (i + 1) / seg;
            double x0 = Math.cos(a0) * rx, y0 = Math.sin(a0) * ry, x1 = Math.cos(a1) * rx, y1 = Math.sin(a1) * ry;
            v(w, cx, cy, 0, c1);
            v(w, cx + x0 * cr - y0 * sr, cy + x0 * sr + y0 * cr, 0, c2);
            v(w, cx + x1 * cr - y1 * sr, cy + x1 * sr + y1 * cr, 0, c2);
        }
    }

    static boolean inEllipse(double px, double py, double cx, double cy, double rx, double ry, double rotDeg) {
        double rot = Math.toRadians(-rotDeg), dx = px - cx, dy = py - cy;
        double x = dx * Math.cos(rot) - dy * Math.sin(rot), y = dx * Math.sin(rot) + dy * Math.cos(rot);
        return (x * x) / (rx * rx) + (y * y) / (ry * ry) <= 1;
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

    static WorldRenderer begin(int mode) {
        WorldRenderer w = Tessellator.getInstance().getWorldRenderer();
        w.begin(mode, DefaultVertexFormats.POSITION_COLOR);
        return w;
    }

    // ---------------- the four wings ----------------
    /** Air: golden armored arms + soft white feathers */
    static void airWings() {
        feathered(9, 70, -38, 1.55, 0.85, 0.28, 0xE8FFFFFF, 0x55D6ECFF);
        feathered(7, 62, -30, 1.15, 0.60, 0.24, 0xF2FFFFFF, 0x88E8F4FF);
        WorldRenderer w = begin(GL11.GL_TRIANGLES);
        double[][] up = bez(0, 0, 0.55, 0.95, 1.30, 0.80, 12);
        double[][] lo = bez(0, 0, 0.80, -0.02, 1.18, -0.52, 12);
        strip(w, up, 0.15, 0.08, 0xFF8D6E00, 0xFF8D6E00);
        strip(w, lo, 0.14, 0.07, 0xFF8D6E00, 0xFF8D6E00);
        strip(w, up, 0.10, 0.04, 0xFFFFC107, 0xFFFFE082);
        strip(w, lo, 0.09, 0.04, 0xFFFFC107, 0xFFFFE082);
        tri(w, 1.30, 0.80, 0xFFFFE082, 1.52, 0.98, 0xFFFFD54F, 1.40, 0.70, 0xFFFFC107);
        tri(w, 1.18, -0.52, 0xFFFFE082, 1.30, -0.78, 0xFFFFD54F, 1.06, -0.60, 0xFFFFC107);
        Tessellator.getInstance().draw();
    }

    /** Fire: pixel-art flame wing with floating sparks */
    static void fireWings(float t) {
        WorldRenderer w = begin(GL11.GL_QUADS);
        double px = 0.075;
        for (int i = 0; i < 27; i++) {
            for (int j = -12; j < 18; j++) {
                double x = 0.12 + i * px, y = j * px;
                double r = Math.sqrt(x * x + y * y), th = Math.atan2(y, x);
                if (th < -1.25 || th > 1.5) continue;
                double flame = Math.max(0, Math.sin(th * 6.0 + t * 3.0));
                double R = 1.05 + 0.45 * flame + 0.35 * Math.cos(th - 0.6);
                if (r > R || r < 0.1) continue;
                double h = hash(i, j), e = r / R;
                if (e > 0.62 && h < (e - 0.62) * 1.9 + 0.1 * Math.sin(t * 5 + h * 20)) continue;
                int c = e < 0.35 ? lerpC(0xFFB71C1C, 0xFFE53935, e / 0.35)
                        : e < 0.7 ? lerpC(0xFFE53935, 0xFFFF9800, (e - 0.35) / 0.35)
                        : lerpC(0xFFFF9800, 0xFFFFEB3B, (e - 0.7) / 0.3);
                if (h > 0.78) c = lerpC(c, 0xFF7F0000, 0.35);
                double s = px * 0.94;
                v(w, x, y, 0, c); v(w, x + s, y, 0, c); v(w, x + s, y + s, 0, c); v(w, x, y + s, 0, c);
            }
        }
        for (int k = 0; k < 16; k++) {
            double rise = (t * 0.35 + hash(k, 3)) % 1.0;
            double sx = 0.35 + hash(k, 1) * 1.5, sy = -0.3 + hash(k, 2) * 0.9 + rise * 0.6, s = 0.035;
            int c = lerpC(0xFFFFEB3B, 0x00FF9800, rise);
            v(w, sx, sy, 0, c); v(w, sx + s, sy, 0, c); v(w, sx + s, sy + s, 0, c); v(w, sx, sy + s, 0, c);
        }
        Tessellator.getInstance().draw();
    }

    /** Water: blue crystal butterfly wings with bubbles */
    static void waterWings(float t) {
        WorldRenderer w = begin(GL11.GL_TRIANGLES);
        fan(w, 0.85, 0.50, 0.85, 0.58, 28, 0xCC1E88E5, 0xEEE3F2FD, 30);
        fan(w, 0.62, -0.18, 0.60, 0.42, -22, 0xCC1565C0, 0xEEBBDEFB, 26);
        for (int k = 0; k < 14; k++) {
            double cx = 0.30 + hash(k, 5) * 1.05, cy = -0.35 + hash(k, 6) * 1.25, r = 0.05 + hash(k, 7) * 0.09;
            boolean in = inEllipse(cx, cy, 0.85, 0.50, 0.85 - r, 0.58 - r, 28) || inEllipse(cx, cy, 0.62, -0.18, 0.60 - r, 0.42 - r, -22);
            if (!in) continue;
            fan(w, cx, cy, r, r, 0, 0xAA1976D2, 0xAA90CAF9, 12);
            fan(w, cx - r * 0.25, cy + r * 0.25, r * 0.35, r * 0.35, 0, 0xCCFFFFFF, 0x00FFFFFF, 8);
        }
        for (int k = 0; k < 10; k++) {
            double sx = 0.2 + hash(k, 11) * 1.7, sy = -0.5 + hash(k, 12) * 1.5;
            double tw = 0.5 + 0.5 * Math.sin(t * 3 + k * 1.7), s = 0.02 + 0.02 * tw;
            int c = ((int) (220 * tw) << 24) | 0xFFFFFF;
            v(w, sx, sy, 0, c); v(w, sx + s, sy, 0, c); v(w, sx + s, sy + s, 0, c);
            v(w, sx, sy, 0, c); v(w, sx + s, sy + s, 0, c); v(w, sx, sy + s, 0, c);
        }
        Tessellator.getInstance().draw();
    }

    /** Earth: leaf wings on a wooden arm */
    static void earthWings() {
        WorldRenderer w = begin(GL11.GL_TRIANGLES);
        for (int i = 0; i < 9; i++) {
            double ang = 75 - i * 12, len = 1.5 - i * 0.07, a = Math.toRadians(ang);
            double cx = Math.cos(a) * len * 0.5 + 0.1, cy = Math.sin(a) * len * 0.5;
            fan(w, cx, cy, len * 0.5, 0.14, ang, lerpC(0xFF1B5E20, 0xFF33691E, i / 8.0), 0xFF9CCC65, 14);
        }
        strip(w, bez(0, 0, 0.5, 0.8, 1.2, 0.9, 10), 0.10, 0.05, 0xFF5D4037, 0xFF8D6E63);
        Tessellator.getInstance().draw();
    }

    static void shape(String id, float t) {
        if (id.equals("w1")) airWings();
        else if (id.equals("w2")) fireWings(t);
        else if (id.equals("w3")) waterWings(t);
        else if (id.equals("w4")) earthWings();
    }

    void drawWings(String id, EntityPlayer p, double x, double y, double z, float pt) {
        float yaw = p.prevRenderYawOffset + (p.renderYawOffset - p.prevRenderYawOffset) * pt;
        boolean sn = p.isSneaking();
        float speed = Config.wingSpeed / 100f;
        float t = (System.currentTimeMillis() % 100000L) / 1000f * speed;
        float flap = (float) Math.sin(t * (p.onGround ? 2.2 : 6.0)) * (p.onGround ? 7 : 16);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-yaw, 0, 1, 0);
        GlStateManager.translate(0, sn ? 1.12 : 1.38, sn ? -0.3 : -0.16);
        if (sn) GlStateManager.rotate(28, 1, 0, 0);
        float s = Config.wingScale;
        GlStateManager.scale(s, s, s);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        for (int side = 0; side < 2; side++) {
            GlStateManager.pushMatrix();
            if (side == 1) GlStateManager.scale(-1f, 1f, 1f);
            GlStateManager.rotate(34 + flap, 0, 1, 0);
            shape(id, t);
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

    // ---------------- the bat pet ----------------
    void drawBat(float t) {
        int body = 0xFF263238, head = 0xFF37474F;
        box(-0.07, 0.00, -0.10, 0.07, 0.13, 0.10, body);
        box(-0.055, 0.10, 0.08, 0.055, 0.21, 0.18, head);
        box(-0.055, 0.21, 0.09, -0.025, 0.30, 0.12, body);
        box(0.025, 0.21, 0.09, 0.055, 0.30, 0.12, body);
        box(-0.04, 0.15, 0.181, -0.015, 0.175, 0.187, 0xFFFF1744);
        box(0.015, 0.15, 0.181, 0.04, 0.175, 0.187, 0xFFFF1744);
        box(-0.02, 0.10, 0.18, -0.01, 0.12, 0.187, 0xFFFFFFFF);
        box(0.01, 0.10, 0.18, 0.02, 0.12, 0.187, 0xFFFFFFFF);
        float flap = 28f * (float) Math.sin(t * 0.9f);
        for (int sd = -1; sd <= 1; sd += 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(sd * 0.06, 0.10, 0.0);
            GlStateManager.scale((float) sd, 1f, 1f);
            GlStateManager.rotate(flap, 0f, 0f, 1f);
            GlStateManager.rotate(90f, 1f, 0f, 0f);
            WorldRenderer w = begin(GL11.GL_TRIANGLES);
            int mem = 0xDD111B21, edge = 0xDD37474F, bn = 0xFF455A64;
            tri(w, 0.0, 0.02, mem, 0.42, 0.12, edge, 0.36, -0.04, edge);
            tri(w, 0.0, 0.02, mem, 0.36, -0.04, edge, 0.26, -0.14, edge);
            tri(w, 0.0, 0.02, mem, 0.26, -0.14, edge, 0.10, -0.12, edge);
            bone(w, 0.0, 0.02, 0.30, 0.10, 0.025, bn);
            bone(w, 0.30, 0.10, 0.42, 0.12, 0.02, bn);
            bone(w, 0.30, 0.10, 0.36, -0.04, 0.02, bn);
            bone(w, 0.30, 0.10, 0.26, -0.14, 0.02, bn);
            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();
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
        GlStateManager.scale(1.5f, 1.5f, 1.5f);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);
        drawBat(t);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
