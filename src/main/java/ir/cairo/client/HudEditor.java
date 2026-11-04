package ir.cairo.client;

import java.io.IOException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

public class HudEditor extends GuiScreen {
    final GuiScreen parent;
    String drag = null;
    int offX, offY;

    public HudEditor(GuiScreen p) { parent = p; }

    void outline(int x, int y, int w, int h, int c) {
        drawRect(x, y, x + w, y + 1, c);
        drawRect(x, y + h - 1, x + w, y + h, c);
        drawRect(x, y, x + 1, y + h, c);
        drawRect(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawRect(0, 0, width, height, 0x55000000);
        FontRenderer fr = fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        for (String id : Hud.ORDER) {
            if (!Config.get(id)) continue;
            int[] r = Hud.position(id, sr, fr, true);
            if (r[2] <= 0) continue;
            Hud.draw(id, r[0], r[1], fr, true);
            boolean hov = id.equals(drag) || (drag == null && Ui.inside(mx, my, r[0], r[1], r[2], r[3]));
            outline(r[0] - 1, r[1] - 1, r[2] + 2, r[3] + 2, hov ? 0xFFFF5252 : 0x88E53935);
        }
        Fa.drawCenter(fr, "ماژول‌ها را با موس بکش  •  ESC = ذخیره و بازگشت", width / 2, height - 36, 0xFFEEEEEE);
        int bx = width / 2 - 60, by = height - 22;
        Ui.button(bx, by, 120, 16, "ریست جایگاه‌ها", Ui.inside(mx, my, bx, by, 120, 16), false);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        if (b != 0) return;
        int bx = width / 2 - 60, by = height - 22;
        if (Ui.inside(mx, my, bx, by, 120, 16)) { Config.clearPos(); return; }
        ScaledResolution sr = new ScaledResolution(mc);
        for (int i = Hud.ORDER.length - 1; i >= 0; i--) {
            String id = Hud.ORDER[i];
            if (!Config.get(id)) continue;
            int[] r = Hud.position(id, sr, fontRendererObj, true);
            if (r[2] > 0 && Ui.inside(mx, my, r[0], r[1], r[2], r[3])) {
                drag = id;
                offX = mx - r[0];
                offY = my - r[1];
                return;
            }
        }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long t) {
        if (drag == null) return;
        ScaledResolution sr = new ScaledResolution(mc);
        int[] r = Hud.position(drag, sr, fontRendererObj, true);
        int sw = sr.getScaledWidth(), sh = sr.getScaledHeight();
        int nx = Math.max(0, Math.min(mx - offX, sw - r[2]));
        int ny = Math.max(0, Math.min(my - offY, sh - r[3]));
        if (nx < 6) nx = 0;
        if (nx + r[2] > sw - 6) nx = sw - r[2];
        if (ny < 6) ny = 0;
        if (ny + r[3] > sh - 6) ny = sh - r[3];
        Config.setPos(drag, nx / (float) sw, ny / (float) sh);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        super.mouseReleased(mx, my, state);
        if (drag != null) { drag = null; Config.save(); }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == Keyboard.KEY_ESCAPE) { Config.save(); mc.displayGuiScreen(parent); return; }
        super.keyTyped(c, k);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
