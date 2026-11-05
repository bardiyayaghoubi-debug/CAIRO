package ir.cairo.client;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class Events {
    final Minecraft mc = Minecraft.getMinecraft();
    boolean applied = false, fbOn = false, zoomOn = false;
    float savedGamma, savedFov, savedSens;
    Set<Integer> pearls = new HashSet<Integer>();
    static final String[] DEATH_WORDS = {"was killed", "was slain", "was shot", "was blown", "was fireballed", "was pummeled",
        "was pricked", "was squashed", "was impaled", "was doomed", "fell ", "died", "burned", "drowned", "blew up",
        "hit the ground", "suffocated", "starved", "tried to swim", "was struck", "withered", "went off with a bang"};

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!applied) {
            applied = true;
            if (Config.get("fpsboost")) FpsBoost.apply();
        }
        if (Config.get("mainmenu") && mc.currentScreen instanceof GuiMainMenu) {
            mc.displayGuiScreen(new CairoMainMenu());
            return;
        }
        Blur.tick(mc);
        Skins.tick(mc);

        // fullbright
        if (Config.get("fullbright")) {
            if (!fbOn) { savedGamma = mc.gameSettings.gammaSetting; fbOn = true; }
            mc.gameSettings.gammaSetting = 100f;
        } else if (fbOn) {
            mc.gameSettings.gammaSetting = savedGamma;
            fbOn = false;
        }

        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;
        Dance.tick(mc);

        // ender pearl: a new pearl spawning right next to us = we threw it
        if (mc.theWorld != null) {
            Set<Integer> cur = new HashSet<Integer>();
            for (Object o : mc.theWorld.loadedEntityList) {
                if (!(o instanceof EntityEnderPearl)) continue;
                Entity en = (Entity) o;
                cur.add(en.getEntityId());
                if (!pearls.contains(en.getEntityId()) && en.getDistanceToEntity(p) < 3.5f) {
                    Hud.pearlEnd = System.currentTimeMillis() + Config.pearlCd * 1000L;
                }
            }
            pearls = cur;
        }

        // zoom (hold C)
        boolean z = Config.get("zoom") && mc.currentScreen == null && CairoClient.zoomKey.isKeyDown();
        if (z && !zoomOn) {
            savedFov = mc.gameSettings.fovSetting;
            savedSens = mc.gameSettings.mouseSensitivity;
            mc.gameSettings.fovSetting = Math.max(10f, savedFov / 4f);
            mc.gameSettings.mouseSensitivity = savedSens * 0.4f;
            zoomOn = true;
        } else if (!z && zoomOn) {
            mc.gameSettings.fovSetting = savedFov;
            mc.gameSettings.mouseSensitivity = savedSens;
            zoomOn = false;
        }

        if (Config.get("sprint") && mc.currentScreen == null && p.moveForward > 0 && !p.isSneaking()
                && !p.isUsingItem() && !p.isCollidedHorizontally && p.getFoodStats().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
        if (p.maxHurtTime > 0 && p.hurtTime > 0 && p.hurtTime >= p.maxHurtTime - 1) Hud.combo = 0;
    }

    @SubscribeEvent
    public void key(InputEvent.KeyInputEvent e) {
        if (CairoClient.menuKey.isPressed() && mc.currentScreen == null) mc.displayGuiScreen(new ClientMenu(null));
        if (CairoClient.danceKey.isPressed() && Config.get("dance") && mc.currentScreen == null) Dance.toggle();
    }

    @SubscribeEvent
    public void mouse(MouseEvent e) {
        if (!e.buttonstate) return;
        long now = System.currentTimeMillis();
        if (e.button == 0) Hud.left.add(now);
        else if (e.button == 1) Hud.right.add(now);
    }

    @SubscribeEvent
    public void attack(AttackEntityEvent e) {
        if (e.entityPlayer == mc.thePlayer && e.target instanceof EntityLivingBase) {
            Hud.combo++;
            if (mc.objectMouseOver != null && mc.objectMouseOver.hitVec != null && mc.thePlayer != null) {
                Vec3 eye = mc.thePlayer.getPositionEyes(1f);
                Hud.reach = mc.objectMouseOver.hitVec.distanceTo(eye);
            }
        }
    }

    @SubscribeEvent
    public void chat(ClientChatReceivedEvent e) {
        if (mc.thePlayer == null) return;
        String t = EnumChatFormatting.getTextWithoutFormattingCodes(e.message.getUnformattedText());
        if (t == null) return;
        String me = mc.thePlayer.getName();
        String t2 = t.replaceFirst("^(\\[[^\\]]*\\]\\s*)+", "");
        boolean mine = t2.startsWith(me + " ");
        if (t.contains("FINAL KILL") && t.contains(me) && !mine) Hud.finals++;
        if (!mine) {
            if (Pattern.compile("\\b(by|fighting)\\s+" + Pattern.quote(me) + "\\b").matcher(t2).find()) Hud.kills++;
        } else {
            for (String w : DEATH_WORDS) {
                if (t2.contains(w)) { Hud.deaths++; break; }
            }
        }
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
        if (e.gui instanceof GuiMainMenu && !Config.get("mainmenu")) {
            e.buttonList.add(new GuiButton(9901, 4, 4, 110, 20, "Cairo Menu"));
            e.buttonList.add(new GuiButton(9902, 4, 26, 110, 20, "Account (Offline)"));
            e.buttonList.add(new GuiButton(9903, 4, 48, 110, 20, "Cosmetics"));
        }
    }

    @SubscribeEvent
    public void act(GuiScreenEvent.ActionPerformedEvent.Post e) {
        if (e.gui instanceof GuiMainMenu) {
            if (e.button.id == 9901) mc.displayGuiScreen(new ClientMenu(e.gui));
            if (e.button.id == 9902) mc.displayGuiScreen(new AccountScreen(e.gui));
            if (e.button.id == 9903) mc.displayGuiScreen(new CosmeticsScreen(e.gui));
        }
    }

    // ---- custom crosshair replaces the vanilla one ----
    @SubscribeEvent
    public void crosshair(RenderGameOverlayEvent.Pre e) {
        if (e.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && Config.get("crosshair") && mc.gameSettings.thirdPersonView == 0) {
            e.setCanceled(true);
            ScaledResolution sr = new ScaledResolution(mc);
            Ui.drawCrosshair(sr.getScaledWidth() / 2, sr.getScaledHeight() / 2);
        }
    }

    void lowHp(ScaledResolution sr) {
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();
        float pulse = 0.65f + 0.35f * (float) Math.sin(System.currentTimeMillis() / 220.0);
        for (int i = 0; i < 12; i++) {
            int a = (int) (110 * pulse * (1f - i / 12f));
            int c = (a << 24) | 0xE53935;
            int t = i * 3;
            Gui.drawRect(0, t, w, t + 3, c);
            Gui.drawRect(0, h - t - 3, w, h - t, c);
            Gui.drawRect(t, t + 3, t + 3, h - t - 3, c);
            Gui.drawRect(w - t - 3, t + 3, w - t, h - t - 3, c);
        }
    }

    @SubscribeEvent
    public void overlay(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.ALL || mc.thePlayer == null || mc.gameSettings.showDebugInfo) return;
        if (mc.currentScreen instanceof HudEditor) return;
        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        float hp = mc.thePlayer.getHealth();
        if (Config.get("lowhp") && hp > 0f && hp <= 6f) lowHp(sr);
        for (String id : Hud.ORDER) {
            if (!Config.get(id)) continue;
            int[] r = Hud.position(id, sr, fr, false);
            if (r[2] <= 0) continue;
            Hud.draw(id, r[0], r[1], fr, false);
        }
    }

    // ---- TNT countdown above primed TNT ----
    @SubscribeEvent
    public void world(RenderWorldLastEvent e) {
        if (!Config.get("tnt") || mc.theWorld == null) return;
        Entity view = mc.getRenderViewEntity();
        if (view == null) return;
        float pt = e.partialTicks;
        double cx = view.lastTickPosX + (view.posX - view.lastTickPosX) * pt;
        double cy = view.lastTickPosY + (view.posY - view.lastTickPosY) * pt;
        double cz = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * pt;
        FontRenderer fr = mc.fontRendererObj;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityTNTPrimed)) continue;
            EntityTNTPrimed t = (EntityTNTPrimed) o;
            double x = t.lastTickPosX + (t.posX - t.lastTickPosX) * pt - cx;
            double y = t.lastTickPosY + (t.posY - t.lastTickPosY) * pt - cy;
            double z = t.lastTickPosZ + (t.posZ - t.lastTickPosZ) * pt - cz;
            double dist = Math.sqrt(x * x + y * y + z * z);
            if (dist > 64) continue;
            float secs = Math.max(0f, (t.fuse - pt) / 20f);
            String s = String.format(Locale.US, "%.1f", secs);
            int col = secs > 2.5f ? 0xFFFFFFFF : secs > 1.2f ? 0xFFFFEB3B : 0xFFFF5252;
            float sc = 0.025f + (float) dist * 0.0018f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y + 1.1f, (float) z);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
            GlStateManager.scale(-sc, -sc, sc);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            int w = fr.getStringWidth(s) / 2;
            Gui.drawRect(-w - 2, -2, w + 2, 9, 0x88000000);
            fr.drawStringWithShadow(s, -w, 0, col);
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.popMatrix();
        }
    }

    // ---- colored block outline ----
    @SubscribeEvent
    public void blockHighlight(DrawBlockHighlightEvent e) {
        if (!Config.get("blockcolor") || e.target == null || e.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        World w = mc.theWorld;
        if (w == null) return;
        BlockPos pos = e.target.getBlockPos();
        Block b = w.getBlockState(pos).getBlock();
        if (b.getMaterial() == Material.air) return;
        e.setCanceled(true);
        b.setBlockBoundsBasedOnState(w, pos);
        EntityPlayer p = e.player;
        double cx = p.lastTickPosX + (p.posX - p.lastTickPosX) * e.partialTicks;
        double cy = p.lastTickPosY + (p.posY - p.lastTickPosY) * e.partialTicks;
        double cz = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * e.partialTicks;
        AxisAlignedBB bb = b.getSelectedBoundingBox(w, pos).expand(0.002, 0.002, 0.002).offset(-cx, -cy, -cz);
        int col = Ui.color(Config.blockColor);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(((col >> 16) & 255) / 255f, ((col >> 8) & 255) / 255f, (col & 255) / 255f, 0.9f);
        GL11.glLineWidth(2.5f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        RenderGlobal.drawSelectionBoundingBox(bb);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
}
