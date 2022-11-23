package net.boatlabs.timing.system.custom.boats.types;

import net.boatlabs.timing.system.custom.boats.CustomBoat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TechnosVolvo extends CustomBoat {
    public static final int customModelData = 11;
    public TechnosVolvo(Player player, Location location) {
        super(player, location, customModelData);
    }
}
