package net.boatlabs.timing.system.custom.boats;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import me.makkuusen.timing.system.ApiUtilities;
import me.makkuusen.timing.system.Database;
import me.makkuusen.timing.system.TPlayer;
import org.bukkit.entity.Player;

@CommandAlias("boat")
public class BoatCommand extends BaseCommand {

    @Default
    public static void onBoat(Player player){
        if (!TimingSystemCustomBoats.isActive(player.getUniqueId())) {
            ApiUtilities.spawnBoatAndAddPlayer(player, player.getLocation());
            return;
        }

        if (!TimingSystemCustomBoats.hasPlayerBoat(player.getUniqueId())) {
            ApiUtilities.spawnBoatAndAddPlayer(player, player.getLocation());
            return;
        }

        CustomBoat customBoat = TimingSystemCustomBoats.createBoat(TimingSystemCustomBoats.getPlayerBoat(player.getUniqueId()).getBoatType(), player, player.getLocation());
        customBoat.spawnBoat();
        TimingSystemCustomBoats.boats.put(customBoat.getBoat().getUniqueId(), customBoat);
    }

    @Subcommand("activate")
    @CommandCompletion("@players")
    public static void onActivate(Player player, @Optional String name) {

        if (name != null && player.isOp()) {
            TPlayer tPlayer = Database.getPlayer(name);
            if (tPlayer == null) {
                player.sendMessage("§cCould not find player");
                return;
            }

            if (TimingSystemCustomBoats.isActive(tPlayer.getUniqueId())) {
                TimingSystemCustomBoats.inActivate(tPlayer.getUniqueId());
                player.sendMessage("§aYou have inactivated custom boats for " + name);
            } else {
                TimingSystemCustomBoats.activate(tPlayer.getUniqueId());
                player.sendMessage("§aYou have activated custom boats for " + name);
            }
            return;
        }

        if (TimingSystemCustomBoats.isActive(player.getUniqueId())) {
            TimingSystemCustomBoats.inActivate(player.getUniqueId());
            player.sendMessage("§aYou have inactivated custom boats");
        } else {
            TimingSystemCustomBoats.activate(player.getUniqueId());
            player.sendMessage("§aYou have activated custom boats");
        }
    }
}
