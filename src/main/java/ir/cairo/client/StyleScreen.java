package ir.cairo.client;

import java.io.IOException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class StyleScreen extends GuiScreen {
    final GuiScreen parent;
    final int pw = 290;

    public StyleScreen(GuiScreen p) { parent = p; }

    int ph() { return 160; }
    int px() { return (width - pw) / 2; }
    int py() { return (height - ph()) / 2; }
    int rx() { return px() + 78; }
    int rw() { return pw - 86; }
    int y1() { return py() + 26; }
    int y2() { return py() + 46; }
    int y3() { return py() + 66; }
    int y4() { return py() + 86; }
    int y5() { return py() + 106; }

    void rowBg(int y, boolean hover) { drawRect(rx(), y, rx() + rw(), y + 16, hover ? Ui.CARD_H : Ui.CARD); }

    void swatches(int y, String label, int selected) {
        rowBg(y, false);
        Fa.drawRight(fontRendererObj, label, rx() + rw() - 6, y + 4, Ui.TXT);
        for (int i = 0; i < 7; i++) {
            int x = rx() + 6 + i * 17;
            if (i == selected) drawRect(x - 1, y, x + 15, y + 16, 0xFFFFFFFF);
            drawRect(x, y + 1, x + 14, y + 15, Ui.color(i));
        }
    }

    void toggleRow(int y, String label, boolean on, int mx, int my) {
        rowBg(y, Ui.inside(mx, my, rx(), y, rw(), 16));
        Fa.drawRight(fontRendererObj, label, rx() + rw() - 6, y + 4, Ui.TXT);
        Ui.toggle(rx() + 6, y + 3, on);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0xAA000000);
        FontRenderer fr = fontRendererObj;
        int px = px(), py = py();
        drawRect(px, py, px + pw, py + ph(), Ui.PANEL);
        drawRect(px, py, px + pw, py + 3, Ui.RED);
        Fa.drawCenter(fr, "ظاهر و جزئیات HUD", px + pw / 2, py + 9, Ui.RED);
        // live preview
        drawRect(px + 8, py + 26, px + 70, py + 120, 0xFF3A3A3A);
        Hud.panel(fr, "FPS: 120", px + 12, py + 34);
        Hud.panel(fr, "CPS: 7 | 3", px + 12, py + 52);
        Hud.panel(fr, "کمبو: 4", px + 12, py + 70);
        swatches(y1(), "رنگ نوار", Config.hudAccent);
        swatches(y2(), "رنگ متن", Config.hudText);
        // background opacity stepper
        rowBg(y3(), false);
        Fa.drawRight(fr, "شفافیت پس‌زمینه", rx() + rw() - 6, y3() + 4, Ui.TXT);
        Ui.button(rx() + 6, y3() + 1, 14, 14, "-", Ui.inside(mx, my, rx() + 6, y3() + 1, 14, 14), false);
        String v = Config.hudBgAlpha + "%";
        fr.drawString(v, rx() + 36 - fr.getStringWidth(v) / 2, y3() + 4, Ui.TXT);
        Ui.button(rx() + 52, y3() + 1, 14, 14, "+", Ui.inside(mx, my, rx() + 52, y3() + 1, 14, 14), false);
        toggleRow(y4(), "پس‌زمینه‌ی ماژول", Config.hudBg, mx, my);
        toggleRow(y5(), "سایه‌ی متن", Config.hudShadow, mx, my);
        Fa.drawCenter(fr, "اندازه‌ی هر ماژول را در «ویرایش HUD» با چرخ موس عوض کن", px + pw / 2, py + ph() - 22, Ui.MUT);
        Fa.drawCenter(fr, "ESC = بازگشت", px + pw / 2, py + ph() - 11, Ui.MUT);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        if (b != 0) return;
        int rx = rx();
        for (int i = 0; i < 7; i++) {
            int x = rx + 6 + i * 17;
            if (Ui.inside(mx, my, x, y1() + 1, 14, 14)) { Config.hudAccent = i; Config.save(); return; }
            if (Ui.inside(mx, my, x, y2() + 1, 14, 14)) { Config.hudText = i; Config.save(); return; }
        }
        if (Ui.inside(mx, my, rx + 6, y3() + 1, 14, 14)) { Config.hudBgAlpha = Math.max(0, Config.hudBgAlpha - 10); Config.save(); return; }
        if (Ui.inside(mx, my, rx + 52, y3() + 1, 14, 14)) { Config.hudBgAlpha = Math.min(100, Config.hudBgAlpha + 10); Config.save(); return; }
        if (Ui.inside(mx, my, rx, y4(), rw(), 16)) { Config.hudBg = !Config.hudBg; Config.save(); return; }
        if (Ui.inside(mx, my, rx, y5(), rw(), 16)) { Config.hudShadow = !Config.hudShadow; Config.save(); }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(c, k);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
