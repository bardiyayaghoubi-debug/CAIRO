package ir.cairo.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;

public class MenuScreen extends GuiScreen {
    final int pw = 240;

    int rowH() { return Math.max(12, Math.min(18, (height - 84) / Config.KEYS.length)); }
    int ph() { return 26 + Config.KEYS.length * rowH() + 4 + 18 + 8; }
    int px() { return (width - pw) / 2; }
    int py() { return (height - ph()) / 2; }

    void button(int x, int y, int w, int h, String text, int mx, int my) {
        boolean hv = mx >= x && mx <= x + w && my >= y && my < y + h;
        drawRect(x, y, x + w, y + h, hv ? 0xFFFF5252 : 0xFFE53935);
        Fa.drawCenter(fontRendererObj, text, x + w / 2, y + (h - 9) / 2, 0xFFFFFFFF);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawRect(0, 0, width, height, 0x99000000);
        int px = px(), py = py(), n = Config.KEYS.length, rh = rowH();
        drawRect(px, py, px + pw, py + ph(), 0xEE2B2B2B);
        drawRect(px, py, px + pw, py + 3, 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "کایرو کلاینت", px + pw / 2, py + 10, 0xFFE53935);
        for (int i = 0; i < n; i++) {
            int y = py + 26 + i * rh;
            String k = Config.KEYS[i];
            boolean on = Config.get(k);
            boolean hover = mx >= px + 8 && mx <= px + pw - 8 && my >= y && my < y + rh - 2;
            drawRect(px + 8, y, px + pw - 8, y + rh - 2, hover ? 0xFF424242 : 0xFF333333);
            String label = Config.LABELS[i];
            if (k.equals("blur")) label += " [" + (Config.blurLevel + 1) + "]  (راست‌کلیک: قدرت)";
            Fa.drawRight(fontRendererObj, label, px + pw - 14, y + (rh - 9) / 2, 0xFFEEEEEE);
            drawRect(px + 14, y + 1, px + 44, y + rh - 3, on ? 0xFFE53935 : 0xFF616161);
            fontRendererObj.drawString(on ? "ON" : "OFF", px + 18, y + (rh - 8) / 2, 0xFFFFFFFF);
        }
        int by = py + 26 + n * rh + 4, half = (pw - 16 - 4) / 2;
        button(px + 8, by, half, 18, "کازمتیک‌ها", mx, my);
        button(px + 8 + half + 4, by, half, 18, "حساب آفلاین", mx, my);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        int px = px(), py = py(), n = Config.KEYS.length, rh = rowH();
        for (int i = 0; i < n; i++) {
            int y = py + 26 + i * rh;
            if (mx >= px + 8 && mx <= px + pw - 8 && my >= y && my < y + rh - 2) {
                String k = Config.KEYS[i];
                if (b == 1 && k.equals("blur")) { Config.blurLevel = (Config.blurLevel + 1) % 3; Config.save(); return; }
                if (b != 0) return;
                boolean v = !Config.get(k);
                Config.set(k, v);
                if (k.equals("fpsboost") && v) FpsBoost.apply();
                return;
            }
        }
        if (b != 0) return;
        int by = py + 26 + n * rh + 4, half = (pw - 16 - 4) / 2;
        if (my >= by && my < by + 18) {
            if (mx >= px + 8 && mx <= px + 8 + half) mc.displayGuiScreen(new CosmeticsScreen(this));
            else if (mx >= px + 8 + half + 4 && mx <= px + pw - 8) mc.displayGuiScreen(new AccountScreen(this));
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
