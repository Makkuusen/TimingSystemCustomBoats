package net.boatlabs.timing.system.custom.boats;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import me.makkuusen.timing.system.ApiUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

@CommandAlias("boat")
public class BoatCommand extends BaseCommand {

    @Default
    public static void onBoat(Player player){
        if (!TimingSystemCustomBoats.hasPlayerBoat(player.getUniqueId())) {
            ApiUtilities.spawnBoatAndAddPlayer(player, player.getLocation());
            return;
        }

        if (player.getResourcePackStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            ApiUtilities.spawnBoatAndAddPlayer(player, player.getLocation());
            return;
        }

        CustomBoat customBoat = TimingSystemCustomBoats.createBoat(TimingSystemCustomBoats.getPlayerBoat(player.getUniqueId()).getBoatType(), player, player.getLocation());
        customBoat.spawnBoat();
        TimingSystemCustomBoats.boats.put(customBoat.getBoat().getUniqueId(), customBoat);
    }

    @Subcommand("select")
    @CommandCompletion("@boatType")
    public static void onBoatSelect(Player player, BoatType boatType) {
        TimingSystemCustomBoats.setPlayerBoat(player.getUniqueId(), boatType);
        player.sendMessage("§aYou have selected " + boatType.name());
    }

    @Subcommand("clear")
    public static void onBoatClear(Player player) {
        if (TimingSystemCustomBoats.removePlayerBoat(player.getUniqueId())) {
            player.sendMessage("§aYour boat has been removed");
            return;
        }
        player.sendMessage("§cYou didn't have any boat selected");
    }
}
