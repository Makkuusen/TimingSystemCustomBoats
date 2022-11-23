package net.boatlabs.timing.system.custom.boats.types;

import net.boatlabs.timing.system.custom.boats.CustomBoat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BasicOrange extends CustomBoat {
    public static final int customModelData = 10;
    public BasicOrange(Player player, Location location) {
        super(player, location, customModelData);
    }
}
