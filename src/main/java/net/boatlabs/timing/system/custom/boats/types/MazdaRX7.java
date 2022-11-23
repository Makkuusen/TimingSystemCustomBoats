package net.boatlabs.timing.system.custom.boats.types;

import net.boatlabs.timing.system.custom.boats.CustomBoat;
import org.bukkit.Location;
import org.bukkit.entity.Player;



public class MazdaRX7 extends CustomBoat {

    public static final int customModelData = 15;
    public MazdaRX7(Player player, Location location) {
        super(player, location, customModelData);
    }
}
