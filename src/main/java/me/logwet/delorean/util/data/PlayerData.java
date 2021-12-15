package me.logwet.delorean.util.data;

public class PlayerData {
    private final String uuid;
    private final String vehicleUUID;
    private double velX = 0.0D;
    private double velY = 0.0D;
    private double velZ = 0.0D;

    public PlayerData(String uuid, String vehicleUUID, double velX, double velY, double velZ) {
        this.uuid = uuid;
        this.vehicleUUID = vehicleUUID;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }

    public String getUUID() {
        return uuid;
    }

    public String getVehicleUUID() {
        return vehicleUUID;
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
