package ir.cairo.client;

import java.util.LinkedList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Events {
    static final int RED = 0xFFE53935, BG = 0xB0262626, TXT = 0xFFEEEEEE;
    final Minecraft mc = Minecraft.getMinecraft();
    final LinkedList<Long> left = new LinkedList<Long>();
    final LinkedList<Long> right = new LinkedList<Long>();
    int combo = 0, finals = 0;
    boolean applied = false;

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!applied) {
            applied = true;
            if (Config.get("fpsboost")) FpsBoost.apply();
        }
        Blur.tick(mc);
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;
        if (Config.get("sprint") && mc.currentScreen == null && p.moveForward > 0 && !p.isSneaking()
                && !p.isUsingItem() && !p.isCollidedHorizontally && p.getFoodStats().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
        if (p.maxHurtTime > 0 && p.hurtTime > 0 && p.hurtTime >= p.maxHurtTime - 1) combo = 0;
    }

    @SubscribeEvent
    public void key(InputEvent.KeyInputEvent e) {
        if (CairoClient.menuKey.isPressed()) mc.displayGuiScreen(new MenuScreen());
    }

    @SubscribeEvent
    public void mouse(MouseEvent e) {
        if (!e.buttonstate) return;
        long now = System.currentTimeMillis();
        if (e.button == 0) left.add(now);
        else if (e.button == 1) right.add(now);
    }

    @SubscribeEvent
    public void attack(AttackEntityEvent e) {
        if (e.entityPlayer == mc.thePlayer && e.target instanceof EntityLivingBase) combo++;
    }

    @SubscribeEvent
    public void chat(ClientChatReceivedEvent e) {
        if (mc.thePlayer == null) return;
        String t = EnumChatFormatting.getTextWithoutFormattingCodes(e.message.getUnformattedText());
        String me = mc.thePlayer.getName();
        if (t != null && t.contains("FINAL KILL") && t.contains(me) && !t.startsWith(me)) finals++;
    }

    @SubscribeEvent
    public void cull(RenderLivingEvent.Pre e) {
        if (Config.get("cull") && mc.thePlayer != null && !(e.entity instanceof EntityPlayer)
                && e.entity.getDistanceToEntity(mc.thePlayer) > 40) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void initGui(GuiScreenEvent.InitGuiEvent.Post e) {
        if (e.gui instanceof GuiMainMenu) {
            e.buttonList.add(new GuiButton(9901, 4, 4, 110, 20, "Cairo Menu"));
            e.buttonList.add(new GuiButton(9902, 4, 26, 110, 20, "Account (Offline)"));
            e.buttonList.add(new GuiButton(9903, 4, 48, 110, 20, "Cosmetics"));
        }
    }

    @SubscribeEvent
    public void act(GuiScreenEvent.ActionPerformedEvent.Post e) {
        if (e.gui instanceof GuiMainMenu) {
            if (e.button.id == 9901) mc.displayGuiScreen(new MenuScreen());
            if (e.button.id == 9902) mc.displayGuiScreen(new AccountScreen(e.gui));
            if (e.button.id == 9903) mc.displayGuiScreen(new CosmeticsScreen(e.gui));
        }
    }

    int line(FontRenderer fr, String s, int y) {
        int w = Fa.width(fr, s) + 8;
        Gui.drawRect(4, y, 4 + w, y + 12, BG);
        Gui.drawRect(4, y, 6, y + 12, RED);
        Fa.draw(fr, s, 9, y + 2, TXT);
        return y + 14;
    }

    int count(Item it) {
        int n = 0;
        for (ItemStack s : mc.thePlayer.inventory.mainInventory)
            if (s != null && s.getItem() == it) n += s.stackSize;
        return n;
    }

    int blocks() {
        int n = 0;
        for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
            if (s != null && s.getItem() instanceof ItemBlock) {
                Block b = ((ItemBlock) s.getItem()).getBlock();
                if (b == Blocks.wool || b == Blocks.stained_hardened_clay || b == Blocks.hardened_clay
                        || b == Blocks.planks || b == Blocks.end_stone || b == Blocks.glass
                        || b == Blocks.stained_glass || b == Blocks.obsidian || b == Blocks.log) n += s.stackSize;
            }
        }
        return n;
    }

    void keyBox(FontRenderer fr, String l, int x, int y, int w, int h, boolean down) {
        Gui.drawRect(x, y, x + w, y + h, down ? 0xE0E53935 : BG);
        fr.drawStringWithShadow(l, x + (w - fr.getStringWidth(l)) / 2f, y + (h - 8) / 2f, TXT);
    }

    @SubscribeEvent
    public void overlay(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.ALL || mc.thePlayer == null || mc.gameSettings.showDebugInfo) return;
        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        long now = System.currentTimeMillis();
        while (!left.isEmpty() && now - left.getFirst() > 1000) left.removeFirst();
        while (!right.isEmpty() && now - right.getFirst() > 1000) right.removeFirst();

        int y = 4;
        if (Config.get("fps")) y = line(fr, "FPS: " + Minecraft.getDebugFPS(), y);
        if (Config.get("ping")) {
            NetworkPlayerInfo i = mc.getNetHandler() != null ? mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()) : null;
            y = line(fr, "پینگ: " + (i != null ? i.getResponseTime() : 0), y);
        }
        if (Config.get("cps")) y = line(fr, "CPS: " + left.size() + " | " + right.size(), y);
        if (Config.get("coords")) y = line(fr, "XYZ: " + MathHelper.floor_double(mc.thePlayer.posX) + ", "
                + MathHelper.floor_double(mc.thePlayer.posY) + ", " + MathHelper.floor_double(mc.thePlayer.posZ), y);
        if (Config.get("combo")) y = line(fr, "کمبو: " + combo, y);
        if (Config.get("finals")) y = line(fr, "فاینال کیل: " + finals, y);
        if (Config.get("resources")) y = line(fr, "آهن " + count(Items.iron_ingot) + "  طلا " + count(Items.gold_ingot)
                + "  الماس " + count(Items.diamond) + "  امرالد " + count(Items.emerald), y);
        if (Config.get("blocks")) y = line(fr, "بلوک: " + blocks(), y);

        if (Config.get("keys")) {
            int ky = sr.getScaledHeight() / 2 + 10;
            keyBox(fr, "W", 26, ky, 20, 20, mc.gameSettings.keyBindForward.isKeyDown());
            keyBox(fr, "A", 4, ky + 22, 20, 20, mc.gameSettings.keyBindLeft.isKeyDown());
            keyBox(fr, "S", 26, ky + 22, 20, 20, mc.gameSettings.keyBindBack.isKeyDown());
            keyBox(fr, "D", 48, ky + 22, 20, 20, mc.gameSettings.keyBindRight.isKeyDown());
            keyBox(fr, "LMB", 4, ky + 44, 31, 18, mc.gameSettings.keyBindAttack.isKeyDown());
            keyBox(fr, "RMB", 37, ky + 44, 31, 18, mc.gameSettings.keyBindUseItem.isKeyDown());
        }

        if (Config.get("armor")) {
            int x = sr.getScaledWidth() - 22;
            int ay = sr.getScaledHeight() / 2 - 40;
            for (int i = 3; i >= 0; i--) {
                ItemStack st = mc.thePlayer.inventory.armorInventory[i];
                if (st == null) continue;
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(st, x, ay);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                if (st.getMaxDamage() > 0) {
                    String d = String.valueOf(st.getMaxDamage() - st.getItemDamage());
                    fr.drawStringWithShadow(d, x - fr.getStringWidth(d) - 2, ay + 4, TXT);
                }
                ay += 18;
            }
            GlStateManager.enableDepth();
        }
    }
}
