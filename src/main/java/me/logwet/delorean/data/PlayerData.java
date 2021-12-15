package me.logwet.delorean.data;

public class PlayerData {
    private final String uuid;
    private final String vehicleUUID;
    private final PositionData position;

    public PlayerData(String uuid, String vehicleUUID, PositionData position) {
        this.uuid = uuid;
        this.vehicleUUID = vehicleUUID;
        this.position = position;
    }

    public String getUUID() {
        return uuid;
    }

    public String getVehicleUUID() {
        return vehicleUUID;
    }

    public PositionData getPosition() {
        return position;
    }
}
