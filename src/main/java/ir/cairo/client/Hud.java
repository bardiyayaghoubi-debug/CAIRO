package ir.cairo.client;

import java.util.LinkedList;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;

public class Hud {
    /** every movable HUD element, in default order */
    public static final String[] ORDER = {"fps", "ping", "cps", "coords", "combo", "reach", "finals", "resources", "blocks", "clock", "direction", "memory", "pearl", "stats", "session", "keys", "armor", "potions"};
    public static int combo = 0, finals = 0;
    public static double reach = 0;
    public static int kills = 0, deaths = 0;
    public static long pearlEnd = 0, sessionStart = System.currentTimeMillis();
    public static final LinkedList<Long> left = new LinkedList<Long>();
    public static final LinkedList<Long> right = new LinkedList<Long>();

    static Minecraft mc() { return Minecraft.getMinecraft(); }

    // ----- style (editable from the client panel) -----
    static int accent() { return Ui.color(Config.hudAccent); }
    static int txt() { return Ui.color(Config.hudText); }
    static int bg() { return Config.hudBg ? (((int) (Config.hudBgAlpha * 2.55f) << 24) | 0x262626) : 0; }

    /** one-line panel in the current style */
    public static void panel(FontRenderer fr, String t, int x, int y) {
        int w = Fa.width(fr, t) + 8;
        if (Config.hudBg) Gui.drawRect(x, y, x + w, y + 12, bg());
        Gui.drawRect(x, y, x + 2, y + 12, accent());
        Fa.draw(fr, t, x + 5, y + 2, txt(), Config.hudShadow);
    }

    static boolean isText(String id) { return !(id.equals("keys") || id.equals("armor") || id.equals("potions")); }

    static int count(Item it) {
        int n = 0;
        for (ItemStack s : mc().thePlayer.inventory.mainInventory)
            if (s != null && s.getItem() == it) n += s.stackSize;
        return n;
    }

    static int blocks() {
        int n = 0;
        for (ItemStack s : mc().thePlayer.inventory.mainInventory) {
            if (s != null && s.getItem() instanceof ItemBlock) {
                Block b = ((ItemBlock) s.getItem()).getBlock();
                if (b == Blocks.wool || b == Blocks.stained_hardened_clay || b == Blocks.hardened_clay
                        || b == Blocks.planks || b == Blocks.end_stone || b == Blocks.glass
                        || b == Blocks.stained_glass || b == Blocks.obsidian || b == Blocks.log) n += s.stackSize;
            }
        }
        return n;
    }

    static String dirName(float yaw) {
        float y = ((yaw % 360f) + 360f) % 360f;
        int i = ((int) (y / 45f + 0.5f)) & 7;
        String[] n = {"جنوب", "جنوب‌غرب", "غرب", "شمال‌غرب", "شمال", "شمال‌شرق", "شرق", "جنوب‌شرق"};
        return n[i];
    }

    static void trim() {
        long now = System.currentTimeMillis();
        while (!left.isEmpty() && now - left.getFirst() > 1000) left.removeFirst();
        while (!right.isEmpty() && now - right.getFirst() > 1000) right.removeFirst();
    }

    public static String text(String id) {
        EntityPlayerSP p = mc().thePlayer;
        if (p == null) return null;
        if (id.equals("fps")) return "FPS: " + Minecraft.getDebugFPS();
        if (id.equals("ping")) {
            NetworkPlayerInfo i = mc().getNetHandler() != null ? mc().getNetHandler().getPlayerInfo(p.getUniqueID()) : null;
            return "پینگ: " + (i != null ? i.getResponseTime() : 0);
        }
        if (id.equals("cps")) { trim(); return "CPS: " + left.size() + " | " + right.size(); }
        if (id.equals("coords")) return "XYZ: " + MathHelper.floor_double(p.posX) + ", " + MathHelper.floor_double(p.posY) + ", " + MathHelper.floor_double(p.posZ);
        if (id.equals("combo")) return "کمبو: " + combo;
        if (id.equals("reach")) return "Reach: " + (reach > 0 ? String.format(Locale.US, "%.2f", reach) : "-");
        if (id.equals("finals")) return "فاینال کیل: " + finals;
        if (id.equals("resources")) return "آهن " + count(Items.iron_ingot) + "  طلا " + count(Items.gold_ingot) + "  الماس " + count(Items.diamond) + "  امرالد " + count(Items.emerald);
        if (id.equals("blocks")) return "بلوک: " + blocks();
        if (id.equals("clock")) return "ساعت: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        if (id.equals("direction")) return "جهت: " + dirName(p.rotationYaw);
        if (id.equals("memory")) {
            Runtime rt = Runtime.getRuntime();
            return "RAM: " + ((rt.totalMemory() - rt.freeMemory()) / 1048576L) + "/" + (rt.maxMemory() / 1048576L) + " MB";
        }
        if (id.equals("pearl")) {
            long left = pearlEnd - System.currentTimeMillis();
            return left > 0 ? "پرل: " + String.format(Locale.US, "%.1f", left / 1000.0) : "پرل: آماده";
        }
        if (id.equals("stats")) {
            String kd = deaths == 0 ? String.valueOf(kills) : String.format(Locale.US, "%.2f", kills / (double) deaths);
            return "کیل " + kills + " | دث " + deaths + " | KD " + kd;
        }
        if (id.equals("session")) {
            long s = (System.currentTimeMillis() - sessionStart) / 1000L;
            return String.format(Locale.US, "زمان: %d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
        }
        return null;
    }

    static int potionCount() {
        EntityPlayerSP p = mc().thePlayer;
        return p == null ? 0 : p.getActivePotionEffects().size();
    }

    static int[] baseSize(String id, FontRenderer fr, boolean preview) {
        if (id.equals("keys")) return new int[]{68, 62};
        if (id.equals("armor")) return new int[]{56, 72};
        if (id.equals("potions")) {
            int n = potionCount();
            if (n == 0 && preview) n = 1;
            return new int[]{104, 16 * n};
        }
        String t = text(id);
        if (t == null) return new int[]{0, 0};
        return new int[]{Fa.width(fr, t) + 8, 12};
    }

    /** size after the module's own scale */
    public static int[] size(String id, FontRenderer fr, boolean preview) {
        int[] b = baseSize(id, fr, preview);
        float s = Config.getScale(id);
        return new int[]{(int) Math.ceil(b[0] * s), (int) Math.ceil(b[1] * s)};
    }

    /** returns {x, y, w, h} in scaled coordinates */
    public static int[] position(String id, ScaledResolution sr, FontRenderer fr, boolean preview) {
        int[] sz = size(id, fr, preview);
        float[] f = Config.getPos(id);
        int x, y;
        int sw = sr.getScaledWidth(), sh = sr.getScaledHeight();
        if (f != null) {
            x = (int) (f[0] * sw);
            y = (int) (f[1] * sh);
        } else if (id.equals("keys")) {
            x = 4; y = sh / 2 + 10;
        } else if (id.equals("armor")) {
            x = sw - 60; y = sh / 2 - 40;
        } else if (id.equals("potions")) {
            x = sw - 108; y = 4;
        } else {
            int idx = 0;
            for (String s : ORDER) {
                if (s.equals(id)) break;
                if (isText(s) && Config.get(s) && Config.getPos(s) == null) idx++;
            }
            x = 4; y = 4 + idx * 14;
        }
        x = Math.max(0, Math.min(x, sw - Math.max(sz[0], 10)));
        y = Math.max(0, Math.min(y, sh - Math.max(sz[1], 10)));
        return new int[]{x, y, sz[0], sz[1]};
    }

    static void keyBox(FontRenderer fr, String l, int x, int y, int w, int h, boolean down) {
        int on = (0xE0 << 24) | (accent() & 0xFFFFFF);
        Gui.drawRect(x, y, x + w, y + h, down ? on : bg());
        fr.drawString(l, x + (w - fr.getStringWidth(l)) / 2f, y + (h - 8) / 2f, txt(), Config.hudShadow);
    }

    public static void draw(String id, int x, int y, FontRenderer fr, boolean preview) {
        float s = Config.getScale(id);
        if (s == 1f) { drawBase(id, x, y, fr, preview); return; }
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 0f);
        GlStateManager.scale(s, s, 1f);
        drawBase(id, 0, 0, fr, preview);
        GlStateManager.popMatrix();
    }

    static void drawBase(String id, int x, int y, FontRenderer fr, boolean preview) {
        EntityPlayerSP p = mc().thePlayer;
        if (p == null) return;
        if (id.equals("keys")) {
            Minecraft m = mc();
            keyBox(fr, "W", x + 22, y, 20, 20, m.gameSettings.keyBindForward.isKeyDown());
            keyBox(fr, "A", x, y + 22, 20, 20, m.gameSettings.keyBindLeft.isKeyDown());
            keyBox(fr, "S", x + 22, y + 22, 20, 20, m.gameSettings.keyBindBack.isKeyDown());
            keyBox(fr, "D", x + 44, y + 22, 20, 20, m.gameSettings.keyBindRight.isKeyDown());
            keyBox(fr, "LMB", x, y + 44, 33, 18, m.gameSettings.keyBindAttack.isKeyDown());
            keyBox(fr, "RMB", x + 35, y + 44, 33, 18, m.gameSettings.keyBindUseItem.isKeyDown());
            return;
        }
        if (id.equals("armor")) {
            for (int i = 3; i >= 0; i--) {
                int yy = y + (3 - i) * 18;
                ItemStack st = p.inventory.armorInventory[i];
                if (st == null) continue;
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                mc().getRenderItem().renderItemAndEffectIntoGUI(st, x + 38, yy);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                if (st.getMaxDamage() > 0) {
                    String d = String.valueOf(st.getMaxDamage() - st.getItemDamage());
                    fr.drawString(d, x + 34 - fr.getStringWidth(d), yy + 4, txt(), Config.hudShadow);
                }
            }
            GlStateManager.enableDepth();
            return;
        }
        if (id.equals("potions")) {
            int yy = y;
            int n = 0;
            for (Object o : p.getActivePotionEffects()) {
                PotionEffect pe = (PotionEffect) o;
                String name = I18n.format(pe.getEffectName());
                if (pe.getAmplifier() > 0) name += " " + (pe.getAmplifier() + 1);
                if (Config.hudBg) Gui.drawRect(x, yy, x + 104, yy + 14, bg());
                Gui.drawRect(x, yy, x + 2, yy + 14, accent());
                fr.drawString(name + " " + Potion.getDurationString(pe), x + 5, yy + 3, txt(), Config.hudShadow);
                yy += 16;
                n++;
            }
            if (n == 0 && preview) {
                if (Config.hudBg) Gui.drawRect(x, y, x + 104, y + 14, bg());
                Gui.drawRect(x, y, x + 2, y + 14, accent());
                fr.drawString("Speed II 1:20", x + 5, y + 3, txt(), Config.hudShadow);
            }
            return;
        }
        String t = text(id);
        if (t == null) return;
        panel(fr, t, x, y);
    }
}
