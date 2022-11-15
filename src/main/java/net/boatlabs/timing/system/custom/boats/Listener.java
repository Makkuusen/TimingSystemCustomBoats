package net.boatlabs.timing.system.custom.boats;

import me.makkuusen.timing.system.TimingSystem;
import me.makkuusen.timing.system.api.events.BoatSpawnEvent;
import me.makkuusen.timing.system.event.EventDatabase;
import me.makkuusen.timing.system.participant.DriverState;
import net.boatlabs.timing.system.custom.boats.types.AdamsMatrix;
import net.boatlabs.timing.system.custom.boats.types.BasicOrange;
import net.boatlabs.timing.system.custom.boats.types.MazdaRX7;
import net.boatlabs.timing.system.custom.boats.types.RallySubaru;
import net.boatlabs.timing.system.custom.boats.types.RenosLegacy;
import net.boatlabs.timing.system.custom.boats.types.TechnosVolvo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.checkerframework.checker.units.qual.C;

public class Listener implements org.bukkit.event.Listener {

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle() instanceof Boat && event.getVehicle().hasMetadata("customboat")) {
            if (event.getExited() instanceof Player player) {
                var maybeDriver = EventDatabase.getDriverFromRunningHeat(player.getUniqueId());
                if (maybeDriver.isPresent()) {
                    if (maybeDriver.get().getState() == DriverState.LOADED) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(TimingSystem.getPlugin(), () -> {
                if (TimingSystemCustomBoats.boats.containsKey(event.getVehicle().getUniqueId())){
                    TimingSystemCustomBoats.boats.get(event.getVehicle().getUniqueId()).remove();
                    TimingSystemCustomBoats.boats.remove(event.getVehicle().getUniqueId());
                }
            }, 10);
        }
    }

    @EventHandler
    public void onBoatSpawnEvent(BoatSpawnEvent event) {
        if (!TimingSystemCustomBoats.playerBoats.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        CustomBoat customBoat = createBoat(TimingSystemCustomBoats.playerBoats.get(event.getPlayer().getUniqueId()), event.getPlayer(), event.getLocation());
        customBoat.spawnBoat();
        event.setBoat(customBoat.getBoat());
        TimingSystemCustomBoats.boats.put(customBoat.getBoat().getUniqueId(), customBoat);
    }

    public static CustomBoat createBoat(BoatType boatType, Player player, Location location){
        switch (boatType) {
            case MazdaRX7 -> {
                return new MazdaRX7(player, location);
            }
            case BasicOrange -> {
                return new BasicOrange(player, location);
            }
            case AdamsMatrix -> {
                return new AdamsMatrix(player, location);
            }
            case RenosLegacy -> {
                return new RenosLegacy(player, location);
            }
            case RallySubaru -> {
                return new RallySubaru(player, location);
            }
            default -> {
                return new TechnosVolvo(player, location);
            }
        }
    }
}
