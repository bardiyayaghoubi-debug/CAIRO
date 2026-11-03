package ir.cairo.client;

import java.io.*;
import java.util.*;

public class Config {
    public static final String[] KEYS = {"fpsboost", "cull", "blur", "fps", "ping", "cps", "coords", "combo", "keys", "armor", "sprint", "resources", "blocks", "finals"};
    public static final String[] LABELS = {"FPS بوست", "حذف موجودات دور", "موشن بلور", "نمایش FPS", "نمایش پینگ", "نمایش CPS", "مختصات", "کمبو", "کیستروکس", "زره", "اسپرینت خودکار", "منابع بدوارز", "تعداد بلوک", "فاینال کیل"};
    static Map<String, Boolean> map = new HashMap<String, Boolean>();
    public static String username = "";
    public static int blurLevel = 1;
    public static String cape = "", wing = "", pet = "";
    public static List<String> licenses = new ArrayList<String>();
    static File file;

    public static void load(File dir) {
        file = new File(dir, "cairo.properties");
        for (String k : KEYS) map.put(k, !k.equals("blur"));
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties p = new Properties();
                p.load(in);
                for (String k : KEYS) if (p.getProperty(k) != null) map.put(k, Boolean.parseBoolean(p.getProperty(k)));
                username = p.getProperty("username", "");
                cape = p.getProperty("cape", "");
                wing = p.getProperty("wing", "");
                pet = p.getProperty("pet", "");
                try { blurLevel = Math.max(0, Math.min(2, Integer.parseInt(p.getProperty("blurlevel", "1")))); } catch (NumberFormatException ignored) {}
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

    public static void save() {
        if (file == null) return;
        try (FileOutputStream out = new FileOutputStream(file)) {
            Properties p = new Properties();
            for (String k : KEYS) p.setProperty(k, String.valueOf(get(k)));
            p.setProperty("username", username);
            p.setProperty("cape", cape);
            p.setProperty("wing", wing);
            p.setProperty("pet", pet);
            p.setProperty("blurlevel", String.valueOf(blurLevel));
            StringBuilder sb = new StringBuilder();
            for (String s : licenses) { if (sb.length() > 0) sb.append(';'); sb.append(s); }
            p.setProperty("licenses", sb.toString());
            p.store(out, "Cairo Client");
        } catch (IOException ignored) {}
    }
}
