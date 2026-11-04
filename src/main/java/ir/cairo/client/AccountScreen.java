package ir.cairo.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class AccountScreen extends GuiScreen {
    GuiScreen parent;
    GuiTextField field;
    String msg = "";

    public AccountScreen(GuiScreen p) { parent = p; }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        field = new GuiTextField(0, fontRendererObj, width / 2 - 90, height / 2 - 10, 180, 20);
        field.setMaxStringLength(16);
        field.setFocused(true);
        field.setText(Account.current());
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public void updateScreen() { field.updateCursorCounter(); }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        if (k == Keyboard.KEY_RETURN) { save(); return; }
        field.textboxKeyTyped(c, k);
    }

    @Override
    protected void mouseClicked(int mx, int my, int b) throws IOException {
        super.mouseClicked(mx, my, b);
        field.mouseClicked(mx, my, b);
        int bx = width / 2 - 90, by = height / 2 + 18;
        if (mx >= bx && mx <= bx + 180 && my >= by && my <= by + 20) save();
    }

    void save() {
        String n = field.getText().trim();
        if (!Account.valid(n)) { msg = "نام نامعتبر است (۳ تا ۱۶ حرف انگلیسی، عدد یا _)"; return; }
        Account.set(n);
        msg = "ذخیره شد: " + n;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Ui.background(this, mx, my, 0xCC000000);
        int cx = width / 2, cy = height / 2;
        drawRect(cx - 110, cy - 50, cx + 110, cy + 70, 0xEE2B2B2B);
        drawRect(cx - 110, cy - 50, cx + 110, cy - 47, 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "حساب آفلاین (کرک)", cx, cy - 38, 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "نام کاربری را وارد کنید", cx, cy - 24, 0xFFBDBDBD);
        field.drawTextBox();
        int bx = cx - 90, by = cy + 18;
        boolean hv = mx >= bx && mx <= bx + 180 && my >= by && my <= by + 20;
        drawRect(bx, by, bx + 180, by + 20, hv ? 0xFFFF5252 : 0xFFE53935);
        Fa.drawCenter(fontRendererObj, "ذخیره", cx, by + 6, 0xFFFFFFFF);
        Fa.drawCenter(fontRendererObj, msg, cx, cy + 44, 0xFFEEEEEE);
        Fa.drawCenter(fontRendererObj, "فقط برای سرورهای آفلاین/کرک", cx, cy + 56, 0xFF9E9E9E);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
