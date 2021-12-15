package me.logwet.delorean.data;

public class PositionData {
    private double x;
    private double y;
    private double z;
    private double velX;
    private double velY;
    private double velZ;

    public PositionData(double x, double y, double z, double velX, double velY, double velZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public double getVelZ() {
        return velZ;
    }
}
