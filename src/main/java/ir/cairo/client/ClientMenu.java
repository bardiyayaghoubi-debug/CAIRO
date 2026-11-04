package ir.cairo.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ClientMenu extends GuiScreen {
    final GuiScreen parent;
    int filter = -1, scroll = 0, maxScroll = 0;
    String msg = "";
    static final int SB = 100, CARD_H = 24, GAP = 4;

    public ClientMenu(GuiScreen p) { parent = p; }

    int pw() { return Math.min(width - 24, 470); }
    int ph() { return Math.min(height - 20, 260); }
    int px() { return (width - pw()) / 2; }
    int py() { return (height - ph()) / 2; }
    int cx() { return px() + SB + 8; }
    int cw() { return pw() - SB - 16; }
    int gridY() { return py() + 50; }
    int gridH() { return ph() - 50 - 8; }
    int cardW() { return (cw() - 6) / 2; }

    List<String[]> list() {
        List<String[]> l = new ArrayList<String[]>();
        for (String[] m : Config.MODS) if (filter < 0 || Integer.parseInt(m[2]) == filter) l.add(m);
        return l;
    }

    int[] card(int i) {
        int col = i % 2, row = i / 2;
        int x = cx() + (1 - col) * (cardW() + 6);
        int y = gridY() + row * (CARD_H + GAP) - scroll;
        return new int[]{x, y, cardW(), CARD_H};
    }

    String[] sideItems() { return new String[]{"ماژول‌ها", "ویرایش HUD", "کراس‌هیر و بلوک", "کازمتیک‌ها", "اسکین", "حساب آفلاین", "بستن"}; }

    int sideY(int i) { return py() + 42 + i * 22; }

    int chipX(int i, FontRenderer fr, String[] chips) {
        int x = cx() + cw();
        for (int k = 0; k <= i; k++) x -= (Fa.width(fr, chips[k]) + 12) + 4;
        return x;
    }

    String[] chips() { return new String[]{"همه", Config.CATS[0], Config.CATS[1], Config.CATS[2], Config.CATS[3]}; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0xAA000000);
        FontRenderer fr = fontRendererObj;
        int px = px(), py = py(), pw = pw(), ph = ph();
        drawRect(px, py, px + pw, py + ph, Ui.PANEL);
        drawRect(px, py, px + pw, py + 2, Ui.RED);
        drawRect(px, py, px + SB, py + ph, Ui.SIDE);
        Ui.logo(px + SB / 2, py + 8, 2f);
        String[] side = sideItems();
        for (int i = 0; i < side.length; i++) {
            int y = sideY(i);
            Ui.button(px + 6, y, SB - 12, 20, side[i], Ui.inside(mx, my, px + 6, y, SB - 12, 20), i == 0);
        }
        // filter chips
        String[] chips = chips();
        for (int i = 0; i < chips.length; i++) {
            int w = Fa.width(fr, chips[i]) + 12;
            int x = chipX(i, fr, chips);
            boolean sel = (filter + 1) == i;
            Ui.button(x, py + 12, w, 16, chips[i], Ui.inside(mx, my, x, py + 12, w, 16), sel);
        }
        // module cards (scrollable)
        List<String[]> l = list();
        int rows = (l.size() + 1) / 2;
        maxScroll = Math.max(0, rows * (CARD_H + GAP) - gridH());
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        Ui.scissor(cx() - 2, gridY(), cw() + 4, gridH());
        for (int i = 0; i < l.size(); i++) {
            int[] c = card(i);
            if (c[1] + c[3] < gridY() || c[1] > gridY() + gridH()) continue;
            String[] m = l.get(i);
            boolean hover = Ui.inside(mx, my, c[0], c[1], c[2], c[3]) && my >= gridY() && my < gridY() + gridH();
            drawRect(c[0], c[1], c[0] + c[2], c[1] + c[3], hover ? Ui.CARD_H : Ui.CARD);
            String label = m[1];
            if (m[0].equals("blur")) label += " [" + (Config.blurLevel + 1) + "]";
            if (m[0].equals("pearl")) label += " [" + Config.pearlCd + "s]";
            Fa.drawRight(fr, label, c[0] + c[2] - 8, c[1] + (c[3] - 9) / 2, Ui.TXT);
            Ui.toggle(c[0] + 8, c[1] + (c[3] - 10) / 2, Config.get(m[0]));
        }
        Ui.noScissor();
        String hint = msg.isEmpty() ? "کلیک = روشن/خاموش  •  راست‌کلیک: قدرت بلور / زمان پرل / ریست آمار" : msg;
        Fa.drawCenter(fr, hint, px + SB + (pw - SB) / 2, py + ph - 10, Ui.MUT);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d != 0) {
            scroll += d > 0 ? -16 : 16;
            scroll = Math.max(0, Math.min(scroll, maxScroll));
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        FontRenderer fr = fontRendererObj;
        int px = px(), py = py();
        if (b == 0) {
            String[] side = sideItems();
            for (int i = 0; i < side.length; i++) {
                if (!Ui.inside(mx, my, px + 6, sideY(i), SB - 12, 20)) continue;
                msg = "";
                if (i == 1) {
                    if (mc.thePlayer == null) msg = "ویرایش HUD فقط داخل بازی ممکن است";
                    else mc.displayGuiScreen(new HudEditor(this));
                } else if (i == 2) mc.displayGuiScreen(new LookScreen(this));
                else if (i == 3) mc.displayGuiScreen(new CosmeticsScreen(this));
                else if (i == 4) mc.displayGuiScreen(new SkinScreen(this));
                else if (i == 5) mc.displayGuiScreen(new AccountScreen(this));
                else if (i == 6) mc.displayGuiScreen(parent);
                return;
            }
            String[] chips = chips();
            for (int i = 0; i < chips.length; i++) {
                int w = Fa.width(fr, chips[i]) + 12;
                if (Ui.inside(mx, my, chipX(i, fr, chips), py + 12, w, 16)) { filter = i - 1; scroll = 0; return; }
            }
        }
        if (my < gridY() || my >= gridY() + gridH()) return;
        List<String[]> l = list();
        for (int i = 0; i < l.size(); i++) {
            int[] c = card(i);
            if (!Ui.inside(mx, my, c[0], c[1], c[2], c[3])) continue;
            String id = l.get(i)[0];
            if (b == 1 && id.equals("blur")) { Config.blurLevel = (Config.blurLevel + 1) % 3; Config.save(); return; }
            if (b == 1 && id.equals("pearl")) {
                int[] v = {5, 10, 15, 20};
                int k = 0;
                for (int q = 0; q < v.length; q++) if (v[q] == Config.pearlCd) k = q;
                Config.pearlCd = v[(k + 1) % v.length];
                Config.save();
                return;
            }
            if (b == 1 && id.equals("stats")) { Hud.kills = 0; Hud.deaths = 0; Hud.finals = 0; return; }
            if (b != 0) return;
            boolean v = !Config.get(id);
            Config.set(id, v);
            if (id.equals("fpsboost") && v) FpsBoost.apply();
            return;
        }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == Keyboard.KEY_ESCAPE || k == Keyboard.KEY_RSHIFT) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(c, k);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
