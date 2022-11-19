package net.boatlabs.timing.system.custom.boats;


import co.aikar.idb.DB;

import java.util.UUID;

public class PlayerBoat {
    UUID uuid;
    BoatType boatType;

    public PlayerBoat(BoatType boatType, UUID uuid) {
        this.uuid = uuid;
        this.boatType = boatType;
        DB.executeUpdateAsync("INSERT INTO `ts_boats` (`uuid`, `boat`) VALUES('" + uuid +  "', '" + boatType.name() + "');");
    }

    public BoatType getBoatType() {
        return boatType;
    }

    public void setBoatType(BoatType boatType) {
        this.boatType = boatType;
        DB.executeUpdateAsync("UPDATE `ts_boats` SET `boat` = '" + boatType.name() + "' WHERE `uuid` = '" + uuid + "';");
    }
}
