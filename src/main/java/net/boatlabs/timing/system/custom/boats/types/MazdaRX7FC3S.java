package net.boatlabs.timing.system.custom.boats.types;

import net.boatlabs.timing.system.custom.boats.CustomBoat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MazdaRX7FC3S extends CustomBoat {

    public static final int customModelData = 16;
    public MazdaRX7FC3S(Player player, Location location) {
        super(player, location, customModelData);
    }
}
