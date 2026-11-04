package ir.cairo.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class ActivateScreen extends GuiScreen {
    final GuiScreen parent;
    GuiTextField field;
    String msg = "";

    public ActivateScreen(GuiScreen p) { parent = p; }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        field = new GuiTextField(0, fontRendererObj, width / 2 - 130, height / 2 - 10, 260, 20);
        field.setMaxStringLength(1000);
        field.setFocused(true);
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public void updateScreen() { field.updateCursorCounter(); }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        if (k == Keyboard.KEY_RETURN) { msg = Cosmetics.activate(field.getText()); return; }
        field.textboxKeyTyped(c, k);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        field.mouseClicked(mx, my, b);
        int bx = width / 2 - 130, by = height / 2 + 18;
        if (mx >= bx && mx <= bx + 260 && my >= by && my <= by + 20) msg = Cosmetics.activate(field.getText());
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0xCC000000);
        int cx = width / 2, cy = height / 2;
        drawRect(cx - 145, cy - 56, cx + 145, cy + 76, 0xEE2B2B2B);
        drawRect(cx - 145, cy - 56, cx + 145, cy - 53, 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "فعال‌سازی کازمتیک", cx, cy - 44, 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "کد را اینجا Paste کن (Ctrl+V)", cx, cy - 28, 0xFFBDBDBD);
        field.drawTextBox();
        int bx = cx - 130, by = cy + 18;
        boolean hv = mx >= bx && mx <= bx + 260 && my >= by && my <= by + 20;
        drawRect(bx, by, bx + 260, by + 20, hv ? 0xFFFF5252 : 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "فعال‌سازی", cx, by + 6, 0xFFFFFFFF);
        Fa.drawCenter(fontRendererObj, msg, cx, cy + 44, 0xFFEEEEEE);
        Fa.drawCenter(fontRendererObj, "نام کاربری فعلی: " + Account.current(), cx, cy + 58, 0xFF9E9E9E);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
