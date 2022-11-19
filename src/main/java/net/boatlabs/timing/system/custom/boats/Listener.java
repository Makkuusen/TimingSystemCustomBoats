package net.boatlabs.timing.system.custom.boats;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import me.makkuusen.timing.system.ItemBuilder;
import me.makkuusen.timing.system.TPlayer;
import me.makkuusen.timing.system.TimingSystem;
import me.makkuusen.timing.system.api.TimingSystemAPI;
import me.makkuusen.timing.system.api.events.BoatSpawnEvent;
import me.makkuusen.timing.system.api.events.GuiOpenEvent;
import me.makkuusen.timing.system.event.EventDatabase;
import me.makkuusen.timing.system.gui.BoatSettingsGui;
import me.makkuusen.timing.system.gui.GuiButton;
import me.makkuusen.timing.system.gui.SettingsGui;
import me.makkuusen.timing.system.participant.DriverState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.UUID;

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
    public void onClickEvent(AsyncPlayerChatEvent event) {
        var key = event.getMessage();
        if (key.equalsIgnoreCase("subscribe2renokas1")) {
            if (TimingSystemCustomBoats.isActive(event.getPlayer().getUniqueId())) {
                TimingSystemCustomBoats.inActivate(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage("§aYou have inactivated Renokas1 custom boats");
            } else {
                TimingSystemCustomBoats.activate(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage("§aYou have activated Renokas1 custom boats");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBoatSpawnEvent(BoatSpawnEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!TimingSystemCustomBoats.hasPlayerBoat(uuid)) {
            return;
        }

        if (!TimingSystemCustomBoats.isActive(event.getPlayer().getUniqueId())) {
            return;
        }

        CustomBoat customBoat = TimingSystemCustomBoats.createBoat(TimingSystemCustomBoats.getPlayerBoat(uuid).getBoatType(), event.getPlayer(), event.getLocation());
        customBoat.spawnBoat();
        event.setBoat(customBoat.getBoat());
        TimingSystemCustomBoats.boats.put(customBoat.getBoat().getUniqueId(), customBoat);
    }

    @EventHandler
    public void onGuiOpenEvent(GuiOpenEvent event) {

        if (!TimingSystemCustomBoats.isActive(event.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getGui() instanceof BoatSettingsGui) {
            event.setCancelled(true);
            var tPlayer = TimingSystemAPI.getTPlayer(event.getPlayer().getUniqueId());
            new CustomBoatSettingsGui(tPlayer).show(tPlayer.getPlayer());

        } else if (event.getGui() instanceof SettingsGui) {
            if (!TimingSystemCustomBoats.hasPlayerBoat(event.getPlayer().getUniqueId())) {
                return;
            }
            var tPlayer = TimingSystemAPI.getTPlayer(event.getPlayer().getUniqueId());
            var gui = event.getGui();
            var playerBoat = TimingSystemCustomBoats.getPlayerBoat(event.getPlayer().getUniqueId());
            gui.setItem(getBoatMenuButton(tPlayer, Material.OAK_BOAT, playerBoat.getBoatType()),14);
        }
    }

    public GuiButton getCustomBoatButton(TPlayer tPlayer, Material material, BoatType boatType){
        var button = new GuiButton(new ItemBuilder(material).setName("§e" + boatType.name()).build());
        button.setAction(()-> {
            TimingSystemCustomBoats.setPlayerBoat(tPlayer.getPlayer().getUniqueId(), boatType);
            new SettingsGui(tPlayer).show(tPlayer.getPlayer());
        });
        return button;
    }

    private static GuiButton getBoatMenuButton(TPlayer tPlayer, Material material, BoatType boatType) {
        var button = new GuiButton(new ItemBuilder(material).setName("§e" + boatType).build());
        button.setAction(() -> {
            new BoatSettingsGui(tPlayer).show(tPlayer.getPlayer());
        });
        return button;
    }
}
