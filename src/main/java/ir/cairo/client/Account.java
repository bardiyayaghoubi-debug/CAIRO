package ir.cairo.client;

import java.nio.charset.Charset;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Account {
    public static boolean valid(String n) {
        return n != null && n.matches("[A-Za-z0-9_]{3,16}");
    }

    public static String current() {
        return Minecraft.getMinecraft().getSession().getUsername();
    }

    public static void set(String n) {
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + n).getBytes(Charset.forName("UTF-8")));
        Session s = new Session(n, id.toString().replace("-", ""), "0", "legacy");
        ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), s, "session", "field_71449_j");
        Config.username = n;
        Cosmetics.invalidate();
        Config.save();
    }

    public static void applySaved() {
        if (valid(Config.username)) set(Config.username);
    }
}
