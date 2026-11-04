package ir.cairo.client;

import java.io.File;
import java.io.IOException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class SkinScreen extends GuiScreen {
    final GuiScreen parent;
    final int pw = 270;
    File[] files = new File[0];
    GuiTextField url;

    public SkinScreen(GuiScreen p) { parent = p; }

    int maxRows() { return Math.max(2, Math.min(8, (height - 170) / 14)); }
    int ph() { return 28 + (maxRows() + 1) * 14 + 6 + 14 + 6 + 18 + 6 + 20 + 6 + 12 + 8; }
    int px() { return (width - pw) / 2; }
    int py() { return (height - ph()) / 2; }
    int rowsY() { return py() + 28; }
    int slimY() { return rowsY() + (maxRows() + 1) * 14 + 6; }
    int btnY() { return slimY() + 14 + 6; }
    int urlY() { return btnY() + 18 + 6; }
    int msgY() { return urlY() + 20 + 6; }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        files = Skins.list();
        url = new GuiTextField(0, fontRendererObj, px() + 8, urlY(), pw - 16 - 70, 18);
        url.setMaxStringLength(400);
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public void updateScreen() { url.updateCursorCounter(); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0xAA000000);
        FontRenderer fr = fontRendererObj;
        int px = px(), py = py();
        drawRect(px, py, px + pw, py + ph(), Ui.PANEL);
        drawRect(px, py, px + pw, py + 3, Ui.RED);
        Fa.drawCenter(fr, "اسکین", px + pw / 2, py + 10, Ui.RED);
        int y0 = rowsY();
        // first row: default
        boolean def = Config.skin == null || Config.skin.isEmpty();
        row(px, y0, "پیش‌فرض (بدون اسکین)", def, mx, my);
        for (int i = 0; i < maxRows() && i < files.length; i++) {
            boolean on = files[i].getName().equals(Config.skin);
            row(px, y0 + (i + 1) * 14, files[i].getName(), on, mx, my);
        }
        if (files.length == 0) Fa.drawRight(fr, "فایل PNG اسکین را در پوشه‌ی اسکین‌ها بگذار", px + pw - 12, y0 + 18, Ui.MUT);
        // slim toggle
        int sy = slimY();
        drawRect(px + 8, sy, px + pw - 8, sy + 12, Ui.inside(mx, my, px + 8, sy, pw - 16, 12) ? Ui.CARD_H : Ui.CARD);
        Fa.drawRight(fr, "بازوی باریک (Alex)", px + pw - 14, sy + 2, Ui.TXT);
        Ui.toggle(px + 14, sy + 1, Config.slim);
        // buttons
        int by = btnY();
        Ui.button(px + 8, by, pw - 16, 18, "باز کردن پوشه‌ی اسکین‌ها", Ui.inside(mx, my, px + 8, by, pw - 16, 18), false);
        url.drawTextBox();
        int dx = px + pw - 8 - 66;
        Ui.button(dx, urlY(), 66, 18, "دانلود", Ui.inside(mx, my, dx, urlY(), 66, 18), true);
        String m = Skins.msg.isEmpty() ? "لینک مستقیم PNG اسکین را در کادر Paste کن" : Skins.msg;
        Fa.drawCenter(fr, m, px + pw / 2, msgY(), Ui.MUT);
        if (mc.thePlayer != null && px >= 64) {
            Ui.drawSpin(px - 36, py + ph() / 2 + 40, 40, (System.currentTimeMillis() / 18L) % 360L, mc.thePlayer);
        }
    }

    void row(int px, int y, String text, boolean on, int mx, int my) {
        boolean hov = Ui.inside(mx, my, px + 8, y, pw - 16, 12);
        drawRect(px + 8, y, px + pw - 8, y + 12, hov ? Ui.CARD_H : Ui.CARD);
        Fa.drawRight(fontRendererObj, text, px + pw - 14, y + 2, on ? Ui.RED : Ui.TXT);
        if (on) fontRendererObj.drawString("ON", px + 14, y + 2, Ui.RED);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        url.mouseClicked(mx, my, b);
        if (b != 0) return;
        int px = px(), y0 = rowsY();
        if (Ui.inside(mx, my, px + 8, y0, pw - 16, 12)) { Skins.clear(); return; }
        for (int i = 0; i < maxRows() && i < files.length; i++) {
            if (Ui.inside(mx, my, px + 8, y0 + (i + 1) * 14, pw - 16, 12)) { Skins.applyFile(files[i]); return; }
        }
        if (Ui.inside(mx, my, px + 8, slimY(), pw - 16, 12)) { Config.slim = !Config.slim; Config.save(); return; }
        if (Ui.inside(mx, my, px + 8, btnY(), pw - 16, 18)) {
            try { java.awt.Desktop.getDesktop().open(Skins.dir()); }
            catch (Throwable t) { Skins.msg = "پوشه: " + Skins.dir().getAbsolutePath(); }
            return;
        }
        int dx = px + pw - 8 - 66;
        if (Ui.inside(mx, my, dx, urlY(), 66, 18)) {
            Skins.downloadUrl(url.getText().trim());
        }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        url.textboxKeyTyped(c, k);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
