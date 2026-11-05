package ir.cairo.client;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Cosmetics {
    public static final String[][] ALL = {
        {"c1", "کیپ سرخ کایرو"}, {"c2", "کیپ نیمه‌شب"}, {"c3", "کیپ شعله"}, {"c4", "کیپ یخ"}, {"c5", "کیپ طلایی"}, {"c6", "کیپ ماتریکس"},
        {"w1", "بال هوا"}, {"w2", "بال آتش"}, {"w3", "بال آب"}, {"w4", "بال زمین"},
        {"p1", "خفاش"}
    };
    /** preview (not owned) per type: 0=cape 1=wing 2=pet */
    public static final String[] pv = new String[3];
    static Set<String> owned = new HashSet<String>();
    static String cacheUser = null;
    static PublicKey pub;

    public static int idx(char t) { return t == 'c' ? 0 : t == 'w' ? 1 : 2; }

    public static void invalidate() { cacheUser = null; }

    static PublicKey key() throws Exception {
        if (pub == null) {
            pub = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(Keys.PUB)));
        }
        return pub;
    }

    static Set<String> expand(String items) {
        Set<String> s = new HashSet<String>();
        if (items.equals("all")) {
            for (String[] c : ALL) s.add(c[0]);
        } else {
            for (String it : items.split("\\+")) for (String[] c : ALL) if (c[0].equals(it)) s.add(it);
        }
        return s;
    }

    static String verify(String user, String code) {
        try {
            int dot = code.indexOf('.');
            if (dot < 1) return null;
            String items = code.substring(0, dot);
            byte[] sig = Base64.getUrlDecoder().decode(code.substring(dot + 1));
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(key());
            s.update(("CAIRO1|" + user + "|" + items).getBytes("UTF-8"));
            return s.verify(sig) ? items : null;
        } catch (Exception e) {
            return null;
        }
    }

    static synchronized Set<String> owned() {
        String u = Account.current().toLowerCase(Locale.ROOT);
        if (!u.equals(cacheUser)) {
            cacheUser = u;
            owned = new HashSet<String>();
            for (String code : Config.licenses) {
                String items = verify(u, code);
                if (items != null) owned.addAll(expand(items));
            }
        }
        return owned;
    }

    public static boolean has(String id) { return id != null && owned().contains(id); }

    public static String equippedId(char t) { return t == 'c' ? Config.cape : t == 'w' ? Config.wing : Config.pet; }

    public static void setEquipped(char t, String id) {
        if (t == 'c') Config.cape = id; else if (t == 'w') Config.wing = id; else Config.pet = id;
        Config.save();
    }

    /** the cosmetic that should currently be shown for a type (preview first, then owned+equipped) */
    public static String effective(char t) {
        String p = pv[idx(t)];
        if (p != null) return p;
        String eq = equippedId(t);
        return (eq != null && !eq.isEmpty() && has(eq)) ? eq : null;
    }

    public static String activate(String raw) {
        String code = raw == null ? "" : raw.replaceAll("\\s", "");
        if (code.isEmpty()) return "کد را وارد کنید";
        String u = Account.current().toLowerCase(Locale.ROOT);
        if (verify(u, code) == null) return "کد نامعتبر است یا برای این نام کاربری نیست";
        if (!Config.licenses.contains(code)) { Config.licenses.add(code); Config.save(); }
        invalidate();
        return "فعال شد";
    }
}
