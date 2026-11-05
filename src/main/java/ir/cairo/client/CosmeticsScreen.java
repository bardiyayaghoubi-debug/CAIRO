package ir.cairo.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

public class CosmeticsScreen extends GuiScreen {
    final GuiScreen parent;
    final int pw = 260;
    final List<String[]> rows = new ArrayList<String[]>();

    public CosmeticsScreen(GuiScreen p) {
        parent = p;
        char last = 0;
        for (String[] c : Cosmetics.ALL) {
            char t = c[0].charAt(0);
            if (t != last) {
                last = t;
                rows.add(new String[]{"", t == 'c' ? "کیپ‌ها" : t == 'w' ? "بال‌ها" : "پت"});
            }
            rows.add(c);
        }
    }

    int rowH() { return Math.max(11, Math.min(15, (height - 140) / rows.size())); }
    int ph() { return 24 + rows.size() * rowH() + 6 + 16 + 3 + 16 + 6 + 18 + 8; }
    int px() { return (width - pw) / 2; }
    int py() { return (height - ph()) / 2; }
    int step1Y() { return py() + 24 + rows.size() * rowH() + 6; }
    int step2Y() { return step1Y() + 19; }
    int btnY() { return step2Y() + 22; }

    @Override
    public void onGuiClosed() { Arrays.fill(Cosmetics.pv, null); }

    void stepper(int y, String label, String value, int mx, int my) {
        int px = px();
        drawRect(px + 8, y, px + pw - 8, y + 16, Ui.CARD);
        Fa.drawRight(fontRendererObj, label, px + pw - 14, y + 4, Ui.TXT);
        Ui.button(px + 14, y + 1, 14, 14, "-", Ui.inside(mx, my, px + 14, y + 1, 14, 14), false);
        fontRendererObj.drawString(value, px + 46 - fontRendererObj.getStringWidth(value) / 2, y + 4, Ui.TXT);
        Ui.button(px + 64, y + 1, 14, 14, "+", Ui.inside(mx, my, px + 64, y + 1, 14, 14), false);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0xAA000000);
        FontRenderer fr = fontRendererObj;
        int px = px(), py = py(), rh = rowH();
        drawRect(px, py, px + pw, py + ph(), Ui.PANEL);
        drawRect(px, py, px + pw, py + 3, Ui.RED);
        Fa.drawCenter(fr, "کازمتیک‌ها", px + pw / 2, py + 10, Ui.RED);
        Fa.draw(fr, "رقص: کلید G", px + 10, py + 10, Ui.MUT);
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            int y = py + 24 + i * rh;
            if (r[0].isEmpty()) {
                Fa.drawRight(fr, r[1], px + pw - 12, y + (rh - 9) / 2, Ui.RED);
                continue;
            }
            String id = r[0];
            char t = id.charAt(0);
            boolean own = Cosmetics.has(id);
            boolean eq = own && id.equals(Cosmetics.equippedId(t));
            boolean pvw = id.equals(Cosmetics.pv[Cosmetics.idx(t)]);
            boolean hover = Ui.inside(mx, my, px + 8, y, pw - 16, rh - 2);
            drawRect(px + 8, y, px + pw - 8, y + rh - 2, hover ? Ui.CARD_H : Ui.CARD);
            Fa.drawRight(fr, r[1], px + pw - 14, y + (rh - 9) / 2, own ? Ui.TXT : Ui.MUT);
            String tag = eq ? "ON" : own ? "USE" : pvw ? "VIEW" : "LOCK";
            int col = eq ? Ui.RED : own ? 0xFF616161 : pvw ? 0xFFB71C1C : 0xFF424242;
            drawRect(px + 14, y + 1, px + 50, y + rh - 3, col);
            fr.drawString(tag, px + 14 + (36 - fr.getStringWidth(tag)) / 2, y + (rh - 8) / 2, 0xFFFFFFFF);
        }
        stepper(step1Y(), "اندازه‌ی بال", Math.round(Config.wingScale * 100) + "%", mx, my);
        stepper(step2Y(), "سرعت بال", Config.wingSpeed + "%", mx, my);
        int by = btnY();
        Ui.button(px + 8, by, pw - 16, 18, "فعال‌سازی کد", Ui.inside(mx, my, px + 8, by, pw - 16, 18), true);
        if (mc.thePlayer != null && px >= 64) {
            Ui.drawSpin(px - 36, py + ph() / 2 + 40, 40, (System.currentTimeMillis() / 18L) % 360L, mc.thePlayer);
        }
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
            if (Ui.inside(mx, my, px + 8, y, pw - 16, rh - 2)) {
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
        int y1 = step1Y(), y2 = step2Y();
        if (Ui.inside(mx, my, px + 14, y1 + 1, 14, 14)) { Config.wingScale = Math.max(0.5f, Math.round(Config.wingScale * 10f) / 10f - 0.1f); Config.save(); return; }
        if (Ui.inside(mx, my, px + 64, y1 + 1, 14, 14)) { Config.wingScale = Math.min(2.0f, Math.round(Config.wingScale * 10f) / 10f + 0.1f); Config.save(); return; }
        if (Ui.inside(mx, my, px + 14, y2 + 1, 14, 14)) { Config.wingSpeed = Math.max(50, Config.wingSpeed - 10); Config.save(); return; }
        if (Ui.inside(mx, my, px + 64, y2 + 1, 14, 14)) { Config.wingSpeed = Math.min(200, Config.wingSpeed + 10); Config.save(); return; }
        int by = btnY();
        if (Ui.inside(mx, my, px + 8, by, pw - 16, 18)) mc.displayGuiScreen(new ActivateScreen(this));
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == 1) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(c, k);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
