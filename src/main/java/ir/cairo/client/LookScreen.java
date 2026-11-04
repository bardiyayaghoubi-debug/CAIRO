package ir.cairo.client;

import java.io.IOException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class LookScreen extends GuiScreen {
    final GuiScreen parent;
    final int pw = 290;
    static final String[] STYLES = {"+", "نقطه", "+ و نقطه"};

    public LookScreen(GuiScreen p) { parent = p; }

    int ph() { return 186; }
    int px() { return (width - pw) / 2; }
    int py() { return (height - ph()) / 2; }
    int rx() { return px() + 78; }
    int rw() { return pw - 86; }
    int yToggle() { return py() + 26; }
    int yStyle() { return py() + 46; }
    int ySize() { return py() + 66; }
    int yGap() { return py() + 86; }
    int yColor() { return py() + 106; }
    int yBlock() { return py() + 134; }
    int yBlockColor() { return py() + 154; }

    void rowBg(int y, boolean hover) {
        drawRect(rx(), y, rx() + rw(), y + 16, hover ? Ui.CARD_H : Ui.CARD);
    }

    void toggleRow(int y, String label, String id, int mx, int my) {
        rowBg(y, Ui.inside(mx, my, rx(), y, rw(), 16));
        Fa.drawRight(fontRendererObj, label, rx() + rw() - 6, y + 4, Ui.TXT);
        Ui.toggle(rx() + 6, y + 3, Config.get(id));
    }

    void stepper(int y, String label, int value, int mx, int my) {
        rowBg(y, false);
        Fa.drawRight(fontRendererObj, label, rx() + rw() - 6, y + 4, Ui.TXT);
        Ui.button(rx() + 6, y + 1, 14, 14, "-", Ui.inside(mx, my, rx() + 6, y + 1, 14, 14), false);
        String v = String.valueOf(value);
        fontRendererObj.drawString(v, rx() + 34 - fontRendererObj.getStringWidth(v) / 2, y + 4, Ui.TXT);
        Ui.button(rx() + 48, y + 1, 14, 14, "+", Ui.inside(mx, my, rx() + 48, y + 1, 14, 14), false);
    }

    void swatches(int y, String label, int selected) {
        rowBg(y, false);
        Fa.drawRight(fontRendererObj, label, rx() + rw() - 6, y + 4, Ui.TXT);
        for (int i = 0; i < 7; i++) {
            int x = rx() + 6 + i * 17;
            if (i == selected) drawRect(x - 1, y, x + 15, y + 16, 0xFFFFFFFF);
            drawRect(x, y + 1, x + 14, y + 15, Ui.color(i));
        }
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0xAA000000);
        FontRenderer fr = fontRendererObj;
        int px = px(), py = py();
        drawRect(px, py, px + pw, py + ph(), Ui.PANEL);
        drawRect(px, py, px + pw, py + 3, Ui.RED);
        Fa.drawCenter(fr, "کراس‌هیر و رنگ بلوک", px + pw / 2, py + 9, Ui.RED);
        // preview
        drawRect(px + 8, py + 26, px + 70, py + 88, 0xFF111111);
        if (Config.get("crosshair")) Ui.drawCrosshair(px + 39, py + 57);
        else Fa.drawCenter(fr, "خاموش", px + 39, py + 54, Ui.MUT);
        toggleRow(yToggle(), "کراس‌هیر سفارشی", "crosshair", mx, my);
        rowBg(yStyle(), Ui.inside(mx, my, rx(), yStyle(), rw(), 16));
        Fa.drawRight(fr, "شکل", rx() + rw() - 6, yStyle() + 4, Ui.TXT);
        Fa.draw(fr, STYLES[Config.chStyle], rx() + 8, yStyle() + 4, Ui.RED);
        stepper(ySize(), "اندازه", Config.chSize, mx, my);
        stepper(yGap(), "فاصله از مرکز", Config.chGap, mx, my);
        swatches(yColor(), "رنگ", Config.chColor);
        toggleRow(yBlock(), "رنگ دور بلوک", "blockcolor", mx, my);
        swatches(yBlockColor(), "رنگ بلوک", Config.blockColor);
        Fa.drawCenter(fr, "رنگ آخر = رنگین‌کمان   •   ESC = بازگشت", px + pw / 2, py + ph() - 12, Ui.MUT);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        if (b != 0) return;
        int rx = rx(), rw = rw();
        if (Ui.inside(mx, my, rx, yToggle(), rw, 16)) { Config.set("crosshair", !Config.get("crosshair")); return; }
        if (Ui.inside(mx, my, rx, yStyle(), rw, 16)) { Config.chStyle = (Config.chStyle + 1) % 3; Config.save(); return; }
        if (Ui.inside(mx, my, rx + 6, ySize() + 1, 14, 14)) { Config.chSize = Math.max(1, Config.chSize - 1); Config.save(); return; }
        if (Ui.inside(mx, my, rx + 48, ySize() + 1, 14, 14)) { Config.chSize = Math.min(14, Config.chSize + 1); Config.save(); return; }
        if (Ui.inside(mx, my, rx + 6, yGap() + 1, 14, 14)) { Config.chGap = Math.max(0, Config.chGap - 1); Config.save(); return; }
        if (Ui.inside(mx, my, rx + 48, yGap() + 1, 14, 14)) { Config.chGap = Math.min(10, Config.chGap + 1); Config.save(); return; }
        for (int i = 0; i < 7; i++) {
            int x = rx + 6 + i * 17;
            if (Ui.inside(mx, my, x, yColor() + 1, 14, 14)) { Config.chColor = i; Config.save(); return; }
            if (Ui.inside(mx, my, x, yBlockColor() + 1, 14, 14)) { Config.blockColor = i; Config.save(); return; }
        }
        if (Ui.inside(mx, my, rx, yBlock(), rw, 16)) { Config.set("blockcolor", !Config.get("blockcolor")); }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(c, k);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
