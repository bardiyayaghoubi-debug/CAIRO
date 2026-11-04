package ir.cairo.client;

import java.io.IOException;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;

public class CairoMainMenu extends GuiScreen {
    static final int BW = 200;
    static final String[] LABELS = {"تک‌نفره", "چندنفره", "ماژول‌ها", "کازمتیک‌ها", "اسکین", "حساب آفلاین", "تنظیمات", "خروج"};

    int top() { return Math.max(6, (height - 200) / 2); }

    int[] rect(int i) {
        int x0 = (width - BW) / 2;
        int half = (BW - 4) / 2;
        int y0 = top() + 56;
        if (i < 2) return new int[]{x0, y0 + i * 25, BW, 22};
        int row = 2 + (i - 2) / 2;
        int y = y0 + row * 25;
        boolean rightSide = (i % 2) == 0;
        return new int[]{rightSide ? x0 + half + 4 : x0, y, half, 22};
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0x66000000);
        FontRenderer fr = fontRendererObj;
        Ui.logo(width / 2, top(), 4f);
        for (int i = 0; i < LABELS.length; i++) {
            int[] r = rect(i);
            Ui.button(r[0], r[1], r[2], r[3], LABELS[i], Ui.inside(mx, my, r[0], r[1], r[2], r[3]), i < 2);
        }
        Fa.draw(fr, "کاربر: " + Account.current(), 6, height - 14, Ui.MUT);
        String v = "Cairo Client 1.0  |  Minecraft 1.8.9";
        fr.drawStringWithShadow(v, width - fr.getStringWidth(v) - 6, height - 12, Ui.MUT);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        if (b != 0) return;
        for (int i = 0; i < LABELS.length; i++) {
            int[] r = rect(i);
            if (!Ui.inside(mx, my, r[0], r[1], r[2], r[3])) continue;
            switch (i) {
                case 0: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
                case 1: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
                case 2: mc.displayGuiScreen(new ClientMenu(this)); break;
                case 3: mc.displayGuiScreen(new CosmeticsScreen(this)); break;
                case 4: mc.displayGuiScreen(new SkinScreen(this)); break;
                case 5: mc.displayGuiScreen(new AccountScreen(this)); break;
                case 6: mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
                case 7: mc.shutdown(); break;
                default: break;
            }
            return;
        }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        // no escape on the main menu
    }
}
