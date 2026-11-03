package ir.cairo.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

public class CosmeticsScreen extends GuiScreen {
    final GuiScreen parent;
    final int pw = 250;
    final List<String[]> rows = new ArrayList<String[]>();

    public CosmeticsScreen(GuiScreen p) {
        parent = p;
        char last = 0;
        for (String[] c : Cosmetics.ALL) {
            char t = c[0].charAt(0);
            if (t != last) {
                last = t;
                rows.add(new String[]{"", t == 'c' ? "کیپ‌ها" : t == 'w' ? "بال‌ها" : "پت‌ها"});
            }
            rows.add(c);
        }
    }

    int rowH() { return Math.max(12, Math.min(16, (height - 80) / rows.size())); }
    int ph() { return 24 + rows.size() * rowH() + 4 + 18 + 8; }
    int px() { return (width - pw) / 2; }
    int py() { return (height - ph()) / 2; }

    @Override
    public void onGuiClosed() { Arrays.fill(Cosmetics.pv, null); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawRect(0, 0, width, height, 0x99000000);
        int px = px(), py = py(), rh = rowH();
        drawRect(px, py, px + pw, py + ph(), 0xEE2B2B2B);
        drawRect(px, py, px + pw, py + 3, 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "کازمتیک‌ها", px + pw / 2, py + 10, 0xFFE53935);
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            int y = py + 24 + i * rh;
            if (r[0].isEmpty()) {
                Fa.drawRight(fontRendererObj, r[1], px + pw - 12, y + (rh - 9) / 2, 0xFFE53935);
                continue;
            }
            String id = r[0];
            char t = id.charAt(0);
            boolean own = Cosmetics.has(id);
            boolean eq = own && id.equals(Cosmetics.equippedId(t));
            boolean pvw = id.equals(Cosmetics.pv[Cosmetics.idx(t)]);
            boolean hover = mx >= px + 8 && mx <= px + pw - 8 && my >= y && my < y + rh - 2;
            drawRect(px + 8, y, px + pw - 8, y + rh - 2, hover ? 0xFF424242 : 0xFF333333);
            Fa.drawRight(fontRendererObj, r[1], px + pw - 14, y + (rh - 9) / 2, own ? 0xFFEEEEEE : 0xFF9E9E9E);
            String tag = eq ? "ON" : own ? "USE" : pvw ? "VIEW" : "LOCK";
            int col = eq ? 0xFFE53935 : own ? 0xFF616161 : pvw ? 0xFFB71C1C : 0xFF424242;
            drawRect(px + 14, y + 1, px + 50, y + rh - 3, col);
            fontRendererObj.drawString(tag, px + 14 + (36 - fontRendererObj.getStringWidth(tag)) / 2, y + (rh - 8) / 2, 0xFFFFFFFF);
        }
        int by = py + 24 + rows.size() * rh + 4;
        boolean hv = mx >= px + 8 && mx <= px + pw - 8 && my >= by && my < by + 18;
        drawRect(px + 8, by, px + pw - 8, by + 18, hv ? 0xFFFF5252 : 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "فعال‌سازی کد", px + pw / 2, by + 5, 0xFFFFFFFF);
        if (mc.thePlayer != null && px >= 64) {
            float spin = (System.currentTimeMillis() / 18L) % 360L;
            drawSpin(px - 36, py + ph() / 2 + 40, 40, spin, mc.thePlayer);
        }
    }

    void drawSpin(int x, int y, int scale, float spin, EntityLivingBase e) {
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

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        if (b != 0) return;
        int px = px(), py = py(), rh = rowH();
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            int y = py + 24 + i * rh;
            if (r[0].isEmpty()) continue;
            if (mx >= px + 8 && mx <= px + pw - 8 && my >= y && my < y + rh - 2) {
                String id = r[0];
                char t = id.charAt(0);
                int ix = Cosmetics.idx(t);
                if (Cosmetics.has(id)) {
                    Cosmetics.pv[ix] = null;
                    Cosmetics.setEquipped(t, id.equals(Cosmetics.equippedId(t)) ? "" : id);
                } else {
                    Cosmetics.pv[ix] = id.equals(Cosmetics.pv[ix]) ? null : id;
                }
                return;
            }
        }
        int by = py + 24 + rows.size() * rh + 4;
        if (mx >= px + 8 && mx <= px + pw - 8 && my >= by && my < by + 18) mc.displayGuiScreen(new ActivateScreen(this));
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == 1) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(c, k);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
