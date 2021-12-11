package me.logwet.delorean.patch.data;

public class PlayerData {
    private String uuid;
    private double velX = 0.0D;
    private double velY = 0.0D;
    private double velZ = 0.0D;

    public PlayerData(String uuid, double velX, double velY, double velZ) {
        this.uuid = uuid;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }

    public String getUuid() {
        return uuid;
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
