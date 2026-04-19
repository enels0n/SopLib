package net.enelson.sopli.lib.customblocks;

public class CustomBlockVisualOptions {

    private final float yaw;
    private final float pitch;
    private final boolean usePitch;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;

    public CustomBlockVisualOptions(float yaw, float pitch, boolean usePitch,
                                    double offsetX, double offsetY, double offsetZ,
                                    float scaleX, float scaleY, float scaleZ) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.usePitch = usePitch;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isUsePitch() {
        return usePitch;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public static CustomBlockVisualOptions of(float yaw, float pitch, boolean usePitch,
                                              double offsetX, double offsetY, double offsetZ,
                                              float scaleX, float scaleY, float scaleZ) {
        return new CustomBlockVisualOptions(yaw, pitch, usePitch, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ);
    }
}
