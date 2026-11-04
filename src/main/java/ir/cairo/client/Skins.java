package ir.cairo.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Skins {
    public static volatile String msg = "";
    static volatile ResourceLocation tex;
    static int counter = 0;
    static Field skinField, typeField;
    static boolean tried, triedSaved, modified;
    static Object lastInfo;
    static ResourceLocation origTex;
    static String origType;

    public static File dir() {
        File d = new File(Minecraft.getMinecraft().mcDataDir, "cairo-skins");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public static File[] list() {
        File[] f = dir().listFiles(new FilenameFilter() {
            public boolean accept(File d, String n) { return n.toLowerCase().endsWith(".png"); }
        });
        if (f == null) f = new File[0];
        Arrays.sort(f);
        return f;
    }

    public static void applyFile(final File f) {
        msg = "در حال بارگذاری...";
        new Thread(new Runnable() {
            public void run() {
                try { finish(ImageIO.read(f), f.getName()); } catch (Exception e) { msg = "خطا در خواندن فایل"; }
            }
        }, "cairo-skin").start();
    }

    public static void downloadUrl(final String url) {
        if (!url.startsWith("http")) { msg = "لینک باید با http شروع شود"; return; }
        msg = "در حال دانلود...";
        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                    c.setRequestProperty("User-Agent", "Mozilla/5.0");
                    c.setConnectTimeout(8000);
                    c.setReadTimeout(8000);
                    BufferedImage img = ImageIO.read(c.getInputStream());
                    if (img == null) { msg = "فایل تصویر معتبر نیست"; return; }
                    File out = new File(dir(), "download_" + (System.currentTimeMillis() / 1000) + ".png");
                    ImageIO.write(img, "png", out);
                    finish(img, out.getName());
                } catch (Exception e) { msg = "دانلود ناموفق بود"; }
            }
        }, "cairo-skin-dl").start();
    }

    static void finish(BufferedImage img, final String name) {
        if (img == null) { msg = "فایل تصویر معتبر نیست"; return; }
        if (img.getWidth() != 64 || (img.getHeight() != 64 && img.getHeight() != 32)) { msg = "اسکین باید 64x64 یا 64x32 باشد"; return; }
        final BufferedImage parsed = new ImageBufferDownload().parseUserSkin(img);
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(new Runnable() {
            public void run() {
                DynamicTexture dt = new DynamicTexture(parsed);
                tex = mc.getTextureManager().getDynamicTextureLocation("cairoskin" + (counter++), dt);
                Config.skin = name;
                Config.save();
                msg = "اسکین اعمال شد";
            }
        });
    }

    public static void clear() {
        tex = null;
        Config.skin = "";
        Config.save();
        msg = "اسکین حذف شد";
    }

    static void fields() {
        if (tried) return;
        tried = true;
        try {
            skinField = ReflectionHelper.findField(NetworkPlayerInfo.class, "locationSkin", "field_178865_e");
        } catch (Exception e) {
            int n = 0;
            for (Field f : NetworkPlayerInfo.class.getDeclaredFields()) {
                if (f.getType() == ResourceLocation.class && ++n == 1) { f.setAccessible(true); skinField = f; break; }
            }
        }
        try {
            typeField = ReflectionHelper.findField(NetworkPlayerInfo.class, "skinType", "field_178863_g");
        } catch (Exception e) {
            for (Field f : NetworkPlayerInfo.class.getDeclaredFields()) {
                if (f.getType() == String.class) { f.setAccessible(true); typeField = f; break; }
            }
        }
    }

    public static void tick(Minecraft mc) {
        if (mc.thePlayer == null || mc.getNetHandler() == null) return;
        if (!triedSaved) {
            triedSaved = true;
            if (Config.skin != null && !Config.skin.isEmpty()) {
                File f = new File(dir(), Config.skin);
                if (f.exists()) applyFile(f);
            }
        }
        fields();
        if (skinField == null || typeField == null) return;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        if (info == null) return;
        try {
            if (info != lastInfo) {
                lastInfo = info;
                origTex = (ResourceLocation) skinField.get(info);
                origType = (String) typeField.get(info);
                modified = false;
            }
            ResourceLocation t = tex;
            if (t != null) {
                if (skinField.get(info) != t) skinField.set(info, t);
                String ty = Config.slim ? "slim" : "default";
                if (!ty.equals(typeField.get(info))) typeField.set(info, ty);
                modified = true;
            } else if (modified) {
                skinField.set(info, origTex);
                typeField.set(info, origType);
                modified = false;
            }
        } catch (Exception ignored) {}
    }
}
