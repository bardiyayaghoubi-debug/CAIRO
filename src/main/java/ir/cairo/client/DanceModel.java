package ir.cairo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;

/** player model that plays the dance while Dance.active */
public class DanceModel extends ModelPlayer {
    final float headY, bodyY, armY, legY;

    public DanceModel(float size, boolean smallArms) {
        super(size, smallArms);
        headY = bipedHead.rotationPointY;
        bodyY = bipedBody.rotationPointY;
        armY = bipedRightArm.rotationPointY;
        legY = bipedRightLeg.rotationPointY;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        if (!Dance.active || entityIn != Minecraft.getMinecraft().thePlayer) return;
        float t = (System.currentTimeMillis() - Dance.start) / 1000f;
        float beat = t * 8.0f;
        float s = (float) Math.sin(beat), c = (float) Math.cos(beat), s2 = (float) Math.sin(beat * 0.5f);
        float bounce = Math.abs(s) * 1.4f;

        bipedHead.rotationPointY = headY - bounce;
        bipedHead.rotateAngleZ = 0.2f * c;
        bipedHead.rotateAngleX = 0.08f;
        bipedHead.rotateAngleY = 0.3f * s2;

        bipedBody.rotationPointY = bodyY - bounce;
        bipedBody.rotateAngleX = 0f;
        bipedBody.rotateAngleY = 0.4f * s2;
        bipedBody.rotateAngleZ = 0.1f * s;

        bipedRightArm.rotationPointY = armY - bounce;
        bipedRightArm.rotateAngleX = -2.6f + 0.5f * s;
        bipedRightArm.rotateAngleY = 0f;
        bipedRightArm.rotateAngleZ = 0.3f + 0.25f * c;

        bipedLeftArm.rotationPointY = armY - bounce;
        bipedLeftArm.rotateAngleX = -2.6f - 0.5f * s;
        bipedLeftArm.rotateAngleY = 0f;
        bipedLeftArm.rotateAngleZ = -0.3f - 0.25f * c;

        bipedRightLeg.rotationPointY = legY - bounce;
        bipedRightLeg.rotateAngleX = 0.7f * s;
        bipedRightLeg.rotateAngleY = 0f;
        bipedRightLeg.rotateAngleZ = 0.08f;

        bipedLeftLeg.rotationPointY = legY - bounce;
        bipedLeftLeg.rotateAngleX = -0.7f * s;
        bipedLeftLeg.rotateAngleY = 0f;
        bipedLeftLeg.rotateAngleZ = -0.08f;

        copyModelAngles(bipedHead, bipedHeadwear);
        copyModelAngles(bipedLeftLeg, bipedLeftLegwear);
        copyModelAngles(bipedRightLeg, bipedRightLegwear);
        copyModelAngles(bipedLeftArm, bipedLeftArmwear);
        copyModelAngles(bipedRightArm, bipedRightArmwear);
        copyModelAngles(bipedBody, bipedBodyWear);
    }
}
