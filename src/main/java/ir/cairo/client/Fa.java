package ir.cairo.client;

import net.minecraft.client.gui.FontRenderer;

public class Fa {
    public static void draw(FontRenderer fr, String s, int x, int y, int color) {
        boolean u = fr.getUnicodeFlag();
        boolean b = fr.getBidiFlag();
        fr.setUnicodeFlag(true);
        fr.setBidiFlag(true);
        fr.drawStringWithShadow(s, x, y, color);
        fr.setUnicodeFlag(u);
        fr.setBidiFlag(b);
    }

    public static int width(FontRenderer fr, String s) {
        boolean u = fr.getUnicodeFlag();
        fr.setUnicodeFlag(true);
        int w = fr.getStringWidth(s);
        fr.setUnicodeFlag(u);
        return w;
    }

    public static void drawRight(FontRenderer fr, String s, int rightX, int y, int color) {
        draw(fr, s, rightX - width(fr, s), y, color);
    }

    public static void drawCenter(FontRenderer fr, String s, int cx, int y, int color) {
        draw(fr, s, cx - width(fr, s) / 2, y, color);
    }
}
