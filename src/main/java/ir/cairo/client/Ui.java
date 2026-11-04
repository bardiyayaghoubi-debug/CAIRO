package ir.cairo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Ui {
    public static final int RED = 0xFFE53935, RED2 = 0xFFFF5252, PANEL = 0xDD1B1B1B, SIDE = 0xEE141414;
    public static final int CARD = 0xFF2E2E2E, CARD_H = 0xFF3C3C3C, TXT = 0xFFEEEEEE, MUT = 0xFF9E9E9E;
    public static final ResourceLocation BG = new ResourceLocation("cairoclient", "textures/gui/bg.png");
    public static final int BGW = 1080, BGH = 625;

    public static final int[] PALETTE = {0xFFFFFFFF, 0xFFE53935, 0xFF43A047, 0xFF00E5FF, 0xFFFFEB3B, 0xFFFF00FF};

    /** palette color; index 6 = animated rainbow */
    public static int color(int idx) {
        if (idx >= 0 && idx < PALETTE.length) return PALETTE[idx];
        return 0xFF000000 | (java.awt.Color.HSBtoRGB((System.currentTimeMillis() % 3000L) / 3000f, 0.9f, 1f) & 0xFFFFFF);
    }

    public static void drawCrosshair(int cx, int cy) {
        int col = color(Config.chColor), s = Config.chSize, g = Config.chGap;
        boolean plus = Config.chStyle != 1, dot = Config.chStyle != 0;
        int sh = 0x99000000;
        if (plus) {
            Gui.drawRect(cx - g - s - 1, cy - 1, cx - g + 1, cy + 2, sh);
            Gui.drawRect(cx + g, cy - 1, cx + 2 + g + s, cy + 2, sh);
            Gui.drawRect(cx - 1, cy - g - s - 1, cx + 2, cy - g + 1, sh);
            Gui.drawRect(cx - 1, cy + g, cx + 2, cy + 2 + g + s, sh);
            Gui.drawRect(cx - g - s, cy, cx - g, cy + 1, col);
            Gui.drawRect(cx + 1 + g, cy, cx + 1 + g + s, cy + 1, col);
            Gui.drawRect(cx, cy - g - s, cx + 1, cy - g, col);
            Gui.drawRect(cx, cy + 1 + g, cx + 1, cy + 1 + g + s, col);
        }
        if (dot) {
            Gui.drawRect(cx - 1, cy - 1, cx + 2, cy + 2, sh);
            Gui.drawRect(cx, cy, cx + 1, cy + 1, col);
        }
    }

    public static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Full-screen background: the Cairo image on menus (no world), plus a dim layer. */
    public static void background(GuiScreen sc, int mx, int my, int dim) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            float s = Math.max((float) sc.width / BGW, (float) sc.height / BGH) * 1.06f;
            int dw = (int) (BGW * s), dh = (int) (BGH * s);
            int x = (sc.width - dw) / 2 + (int) (-(mx - sc.width / 2f) * 0.012f);
            int y = (sc.height - dh) / 2 + (int) (-(my - sc.height / 2f) * 0.012f);
            mc.getTextureManager().bindTexture(BG);
            GlStateManager.disableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.color(1f, 1f, 1f, 1f);
            Gui.drawScaledCustomSizeModalRect(x, y, 0, 0, BGW, BGH, dw, dh, BGW, BGH);
        }
        Gui.drawRect(0, 0, sc.width, sc.height, dim);
    }

    public static void button(int x, int y, int w, int h, String text, boolean hover, boolean primary) {
        int c = primary ? (hover ? RED2 : RED) : (hover ? CARD_H : CARD);
        Gui.drawRect(x, y, x + w, y + h, c);
        Fa.drawCenter(Minecraft.getMinecraft().fontRendererObj, text, x + w / 2, y + (h - 9) / 2, 0xFFFFFFFF);
    }

    public static void toggle(int x, int y, boolean on) {
        Gui.drawRect(x, y, x + 22, y + 10, on ? RED : 0xFF555555);
        Gui.drawRect(on ? x + 13 : x + 1, y + 1, on ? x + 21 : x + 9, y + 9, 0xFFFFFFFF);
    }

    public static void scissor(int x, int y, int w, int h) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int f = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * f, (sr.getScaledHeight() - (y + h)) * f, w * f, h * f);
    }

    public static void noScissor() { GL11.glDisable(GL11.GL_SCISSOR_TEST); }

    public static void logo(int cx, int y, float scale) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) cx, (float) y, 0f);
        GlStateManager.scale(scale, scale, 1f);
        fr.drawStringWithShadow("CAIRO", -fr.getStringWidth("CAIRO") / 2f, 0, RED);
        GlStateManager.popMatrix();
        String sub = "C L I E N T";
        fr.drawStringWithShadow(sub, cx - fr.getStringWidth(sub) / 2f, y + 9 * scale + 2, 0xFFBDBDBD);
    }

    public static void drawSpin(int x, int y, int scale, float spin, EntityLivingBase e) {
        Minecraft mc = Minecraft.getMinecraft();
        float rbo = e.renderYawOffset, ry = e.rotationYaw, rp = e.rotationPitch, prh = e.prevRotationYawHead, rh = e.rotationYawHead;
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 50.0F);
        GlStateManager.scale((float) -scale, (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        RenderHelper.enableStandardItemLighting();
        e.renderYawOffset = spin;
        e.rotationYaw = spin;
        e.prevRotationYawHead = spin;
        e.rotationYawHead = spin;
        e.rotationPitch = 0;
        RenderManager rm = mc.getRenderManager();
        rm.setPlayerViewY(180.0F);
        rm.setRenderShadow(false);
        rm.renderEntityWithPosYaw(e, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rm.setRenderShadow(true);
        e.renderYawOffset = rbo;
        e.rotationYaw = ry;
        e.rotationPitch = rp;
        e.prevRotationYawHead = prh;
        e.rotationYawHead = rh;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
