package net.boatlabs.timing.system.custom.boats.types;

import net.boatlabs.timing.system.custom.boats.CustomBoat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RenosLegacy extends CustomBoat {
    public static final int customModelData = 14;
    public RenosLegacy(Player player, Location location) {
        super(player, location, customModelData);
    }
}
