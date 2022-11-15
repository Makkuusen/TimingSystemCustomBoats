package net.boatlabs.timing.system.custom.boats;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.idb.DB;
import net.boatlabs.timing.system.custom.boats.types.TechnosVolvo;
import org.bukkit.entity.Player;

@CommandAlias("boat")
public class BoatCommand extends BaseCommand {

    @Default
    public static void onBoat(Player player){
        var newBoat = new TechnosVolvo(player, player.getLocation());
        newBoat.spawnBoat();
        TimingSystemCustomBoats.boats.put(newBoat.getBoat().getUniqueId(), newBoat);
    }

    @Subcommand("select")
    @CommandCompletion("@boatType")
    public static void onBoatSelect(Player player, BoatType boatType) {
        if (TimingSystemCustomBoats.playerBoats.containsKey(player.getUniqueId())) {
            DB.executeUpdateAsync("UPDATE `ts_boats` SET `boat` = '" + boatType.name() + "' WHERE `uuid` = '" + player.getUniqueId() + "';");
        } else {
            DB.executeUpdateAsync("INSERT INTO `ts_boats` (`uuid`, `boat`) VALUES('" + player.getUniqueId() +  "', '" + boatType.name() + "');");
        }
        TimingSystemCustomBoats.playerBoats.put(player.getUniqueId(), boatType);
        player.sendMessage("§aYou have selected " + boatType.name());
        DB.executeUpdateAsync("");
    }

    @Subcommand("clear")
    public static void onBoatClear(Player player) {
        if (TimingSystemCustomBoats.playerBoats.containsKey(player.getUniqueId())) {
            TimingSystemCustomBoats.playerBoats.remove(player.getUniqueId());
            player.sendMessage("§aYour boat has been removed");
            DB.executeUpdateAsync("DELETE FROM `ts_boats` WHERE `uuid` = '" + player.getUniqueId() + "';");
            return;
        }
        player.sendMessage("§cYou didn't have any boat selected");
    }
}
