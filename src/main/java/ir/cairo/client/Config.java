package ir.cairo.client;

import java.io.*;
import java.util.*;

public class Config {
    /** id, Persian label, category (0=graphics 1=pvp 2=bedwars 3=info) */
    public static final String[][] MODS = {
        {"fpsboost", "FPS بوست", "0"}, {"cull", "حذف موجودات دور", "0"}, {"blur", "موشن بلور", "0"},
        {"fullbright", "روشنایی کامل", "0"}, {"mainmenu", "منوی اصلی کایرو", "0"},
        {"cps", "نمایش CPS", "1"}, {"combo", "کمبو", "1"}, {"reach", "نمایش Reach", "1"}, {"keys", "کیستروکس", "1"},
        {"armor", "نمایش زره", "1"}, {"potions", "افکت‌های پوشن", "1"}, {"sprint", "اسپرینت خودکار", "1"}, {"zoom", "زوم (کلید C)", "1"},
        {"dance", "رقص (کلید G)", "1"},
        {"resources", "منابع بدوارز", "2"}, {"blocks", "تعداد بلوک", "2"}, {"finals", "فاینال کیل", "2"},
        {"fps", "نمایش FPS", "3"}, {"ping", "نمایش پینگ", "3"}, {"coords", "مختصات", "3"}, {"clock", "ساعت", "3"},
        {"direction", "جهت", "3"}, {"memory", "مصرف رم", "3"},
        {"tnt", "تایمر TNT", "2"}, {"pearl", "تایمر پرل", "2"}, {"crosshair", "کراس‌هیر سفارشی", "1"},
        {"blockcolor", "رنگ دور بلوک", "0"}, {"stats", "آمار کیل/دث", "1"}, {"session", "زمان بازی", "3"}, {"lowhp", "هشدار جان کم", "1"}
    };
    public static final String[] CATS = {"گرافیک", "PvP", "بدوارز", "اطلاعات"};

    static Map<String, Boolean> map = new HashMap<String, Boolean>();
    static Map<String, float[]> pos = new HashMap<String, float[]>();
    static Map<String, Float> scales = new HashMap<String, Float>();
    public static String username = "", cape = "", wing = "", pet = "", skin = "";
    public static boolean slim = false;
    public static int blurLevel = 1;
    public static int chStyle = 0, chSize = 5, chGap = 3, chColor = 0, blockColor = 1, pearlCd = 15;
    public static float wingScale = 1.0f;
    public static int wingSpeed = 100;
    public static int hudAccent = 1, hudText = 0, hudBgAlpha = 70;
    public static boolean hudBg = true, hudShadow = true;
    public static List<String> licenses = new ArrayList<String>();
    static File file;

    static int ci(Properties p, String k, int def, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(p.getProperty(k, String.valueOf(def))))); }
        catch (NumberFormatException e) { return def; }
    }

    public static String label(String id) {
        for (String[] m : MODS) if (m[0].equals(id)) return m[1];
        return id;
    }

    static boolean defaultOn(String id) { return !(id.equals("blur") || id.equals("fullbright")); }

    public static void load(File dir) {
        file = new File(dir, "cairo.properties");
        for (String[] m : MODS) map.put(m[0], defaultOn(m[0]));
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties p = new Properties();
                p.load(in);
                for (String[] m : MODS) {
                    String k = m[0];
                    if (p.getProperty(k) != null) map.put(k, Boolean.parseBoolean(p.getProperty(k)));
                    String ps = p.getProperty("pos." + k);
                    if (ps != null) {
                        try {
                            String[] a = ps.split(",");
                            pos.put(k, new float[]{Float.parseFloat(a[0]), Float.parseFloat(a[1])});
                        } catch (Exception ignored) {}
                    }
                    String sc = p.getProperty("scale." + k);
                    if (sc != null) {
                        try { scales.put(k, Float.parseFloat(sc)); } catch (Exception ignored) {}
                    }
                }
                username = p.getProperty("username", "");
                cape = p.getProperty("cape", "");
                wing = p.getProperty("wing", "");
                pet = p.getProperty("pet", "");
                skin = p.getProperty("skin", "");
                slim = Boolean.parseBoolean(p.getProperty("slim", "false"));
                blurLevel = ci(p, "blurlevel", 1, 0, 2);
                chStyle = ci(p, "chstyle", 0, 0, 2);
                chSize = ci(p, "chsize", 5, 1, 14);
                chGap = ci(p, "chgap", 3, 0, 10);
                chColor = ci(p, "chcolor", 0, 0, 6);
                blockColor = ci(p, "blockcolor", 1, 0, 6);
                pearlCd = ci(p, "pearlcd", 15, 1, 60);
                wingScale = ci(p, "wingscale", 100, 50, 200) / 100f;
                wingSpeed = ci(p, "wingspeed", 100, 50, 200);
                hudAccent = ci(p, "hudaccent", 1, 0, 6);
                hudText = ci(p, "hudtext", 0, 0, 6);
                hudBgAlpha = ci(p, "hudbgalpha", 70, 0, 100);
                hudBg = Boolean.parseBoolean(p.getProperty("hudbg", "true"));
                hudShadow = Boolean.parseBoolean(p.getProperty("hudshadow", "true"));
                licenses.clear();
                for (String s : p.getProperty("licenses", "").split(";")) if (!s.trim().isEmpty()) licenses.add(s.trim());
            } catch (IOException ignored) {}
        }
    }

    public static boolean get(String k) {
        Boolean b = map.get(k);
        return b != null && b;
    }

    public static void set(String k, boolean v) {
        map.put(k, v);
        save();
    }

    public static float[] getPos(String id) { return pos.get(id); }

    public static void setPos(String id, float fx, float fy) { pos.put(id, new float[]{fx, fy}); }

    public static void clearPos() { pos.clear(); scales.clear(); save(); }

    public static float getScale(String id) {
        Float f = scales.get(id);
        return f == null ? 1f : f;
    }

    public static void setScale(String id, float s) {
        scales.put(id, Math.max(0.5f, Math.min(2.5f, Math.round(s * 10f) / 10f)));
    }

    public static void save() {
        if (file == null) return;
        try (FileOutputStream out = new FileOutputStream(file)) {
            Properties p = new Properties();
            for (String[] m : MODS) {
                p.setProperty(m[0], String.valueOf(get(m[0])));
                float[] f = pos.get(m[0]);
                if (f != null) p.setProperty("pos." + m[0], f[0] + "," + f[1]);
                Float sc = scales.get(m[0]);
                if (sc != null) p.setProperty("scale." + m[0], String.valueOf(sc));
            }
            p.setProperty("username", username);
            p.setProperty("cape", cape);
            p.setProperty("wing", wing);
            p.setProperty("pet", pet);
            p.setProperty("skin", skin);
            p.setProperty("slim", String.valueOf(slim));
            p.setProperty("blurlevel", String.valueOf(blurLevel));
            p.setProperty("chstyle", String.valueOf(chStyle));
            p.setProperty("chsize", String.valueOf(chSize));
            p.setProperty("chgap", String.valueOf(chGap));
            p.setProperty("chcolor", String.valueOf(chColor));
            p.setProperty("blockcolor", String.valueOf(blockColor));
            p.setProperty("pearlcd", String.valueOf(pearlCd));
            p.setProperty("wingscale", String.valueOf(Math.round(wingScale * 100f)));
            p.setProperty("wingspeed", String.valueOf(wingSpeed));
            p.setProperty("hudaccent", String.valueOf(hudAccent));
            p.setProperty("hudtext", String.valueOf(hudText));
            p.setProperty("hudbgalpha", String.valueOf(hudBgAlpha));
            p.setProperty("hudbg", String.valueOf(hudBg));
            p.setProperty("hudshadow", String.valueOf(hudShadow));
            StringBuilder sb = new StringBuilder();
            for (String s : licenses) { if (sb.length() > 0) sb.append(';'); sb.append(s); }
            p.setProperty("licenses", sb.toString());
            p.store(out, "Cairo Client");
        } catch (IOException ignored) {}
    }
}
