package ir.cairo.client;

import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

public class Blur {
    static boolean active = false;
    static int fails = 0, lastLevel = -1;
    static ShaderGroup lastGroup;
    static final float[] LV = {0.6f, 0.8f, 0.92f};
    static Field listField;

    @SuppressWarnings("unchecked")
    static List<Shader> shaders(ShaderGroup g) {
        try {
            if (listField == null) {
                for (Field f : ShaderGroup.class.getDeclaredFields()) {
                    if (List.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        List<?> l = (List<?>) f.get(g);
                        if (l != null && !l.isEmpty() && l.get(0) instanceof Shader) { listField = f; break; }
                    }
                }
            }
            return listField == null ? null : (List<Shader>) listField.get(g);
        } catch (Exception e) {
            return null;
        }
    }

    public static void tick(Minecraft mc) {
        boolean want = Config.get("blur") && mc.theWorld != null && OpenGlHelper.shadersSupported;
        if (want) {
            ShaderGroup g = mc.entityRenderer.getShaderGroup();
            if (g == null || !g.getShaderGroupName().contains("phosphor")) {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/phosphor.json"));
                g = mc.entityRenderer.getShaderGroup();
                if (g == null) {
                    if (++fails > 3) { Config.set("blur", false); fails = 0; }
                    return;
                }
            }
            fails = 0;
            active = true;
            if (g != lastGroup || lastLevel != Config.blurLevel) {
                List<Shader> l = shaders(g);
                if (l != null) {
                    float f = LV[Config.blurLevel % 3];
                    for (Shader s : l) {
                        ShaderUniform u = s.getShaderManager().getShaderUniform("Phosphor");
                        if (u != null) u.set(f, f, f);
                    }
                }
                lastGroup = g;
                lastLevel = Config.blurLevel;
            }
        } else if (active) {
            mc.entityRenderer.stopUseShader();
            active = false;
            lastGroup = null;
        }
    }
}
