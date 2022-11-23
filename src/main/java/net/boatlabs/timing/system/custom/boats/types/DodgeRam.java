package net.boatlabs.timing.system.custom.boats.types;

import net.boatlabs.timing.system.custom.boats.CustomBoat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DodgeRam extends CustomBoat {
    public static final int customModelData = 17;
    public DodgeRam(Player player, Location location) {
        super(player, location, customModelData);
    }
}
