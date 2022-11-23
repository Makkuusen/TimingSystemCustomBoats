package net.boatlabs.timing.system.custom.boats;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.contexts.ContextResolver;
import co.aikar.idb.BukkitDB;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import me.makkuusen.timing.system.TimingSystem;
import me.makkuusen.timing.system.track.Track;
import net.boatlabs.timing.system.custom.boats.types.AdamsMatrix;
import net.boatlabs.timing.system.custom.boats.types.BasicOrange;
import net.boatlabs.timing.system.custom.boats.types.DodgeRam;
import net.boatlabs.timing.system.custom.boats.types.MazdaRX7;
import net.boatlabs.timing.system.custom.boats.types.MazdaRX7FC3S;
import net.boatlabs.timing.system.custom.boats.types.RallySubaru;
import net.boatlabs.timing.system.custom.boats.types.RenosLegacy;
import net.boatlabs.timing.system.custom.boats.types.TechnosVolvo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TimingSystemCustomBoats extends JavaPlugin {

    private static HashMap<UUID, PlayerBoat> playerBoats = new HashMap<>();
    public static HashMap<UUID, CustomBoat> boats = new HashMap<>();
    public static Set<UUID> active = new HashSet<>();

    public void onEnable() {
        this.getLogger().info("TimingSystemMedals loaded");
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Listener(), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        // enable brigadier integration for paper servers
        manager.enableUnstableAPI("brigadier");
        manager.registerCommand(new BoatCommand());

        manager.getCommandContexts().registerContext(
                BoatType.class, getBoatTypeModeContextResolver());
        manager.getCommandCompletions().registerAsyncCompletion("boatType", context -> {
            List<String> res = new ArrayList<>();
            for (BoatType type : BoatType.values()){
                res.add(type.name());
            }
            return res;
        });

        initialize();
        createTables();
        synchronize();
    }

    public void onDisable() {
        DB.close();
    }

    public boolean createTables() {
        try {
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_boats` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `boat` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_boats_active` (\n" +
                    "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',\n" +
                    "  PRIMARY KEY (`uuid`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            return true;
        } catch (SQLException exception){
            this.getLogger().warning("Failed to create database tables, disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    public boolean synchronize() {
        try {
            var result = DB.getResults("SELECT * FROM `ts_boats`");
            for (DbRow row : result) {
                UUID uuid = UUID.fromString(row.getString("uuid"));
                BoatType type = row.getString("boat") == null ? BoatType.TechnosVolvo : BoatType.valueOf(row.getString("boat"));
                var playerBoat = new PlayerBoat(type, uuid, false);
                TimingSystemCustomBoats.playerBoats.put(uuid, playerBoat);
            }

            result = DB.getResults("SELECT * FROM `ts_boats_active`");
            for (DbRow row : result) {
                UUID uuid = UUID.fromString(row.getString("uuid"));
                active.add(uuid);
            }
            return true;
        } catch (SQLException exception){
            this.getLogger().warning("Could not synchronize database, disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    public boolean initialize() {
        try {
            BukkitDB.createHikariDatabase(TimingSystem.getPlugin(),
                    TimingSystem.configuration.getSqlUsername(),
                    TimingSystem.configuration.getSqlPassword(),
                    TimingSystem.configuration.getSqlDatabase(),
                    TimingSystem.configuration.getSqlHost() + ":" + TimingSystem.configuration.getSqlPort()
            );
            return createTables();
        } catch (Exception e) {
            this.getLogger().warning("Failed to initialize database, disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    public static ContextResolver<BoatType, BukkitCommandExecutionContext> getBoatTypeModeContextResolver() {
        return (c) -> {
            String name = c.popFirstArg();
            try {
                return BoatType.valueOf(name);
            } catch (IllegalArgumentException e) {
                throw new InvalidCommandArgument(MessageKeys.INVALID_SYNTAX);
            }
        };
    }

    public static @Nullable PlayerBoat getPlayerBoat(UUID uuid){
        var playerBoat = playerBoats.get(uuid);
        return playerBoat;
    }

    public static void setPlayerBoat(UUID uuid, BoatType boatType) {
        PlayerBoat playerBoat;
        if (playerBoats.containsKey(uuid)) {
            playerBoat = playerBoats.get(uuid);
            playerBoat.setBoatType(boatType);
        } else {
            playerBoat = new PlayerBoat(boatType, uuid, true);
        }
        playerBoats.put(uuid, playerBoat);
    }

    public static boolean removePlayerBoat(UUID uuid) {
        if (playerBoats.containsKey(uuid)) {
            playerBoats.remove(uuid);
            DB.executeUpdateAsync("DELETE FROM `ts_boats` WHERE `uuid` = '" + uuid + "';");
            return true;
        }
        return false;
    }

    public static boolean hasPlayerBoat(UUID uuid) {
        return playerBoats.containsKey(uuid);
    }

    public static boolean isActive(UUID uuid) {
        return active.contains(uuid);
    }

    public static void inActivate(UUID uuid) {
        active.remove(uuid);
        DB.executeUpdateAsync("DELETE FROM `ts_boats_active` WHERE `uuid` = '" + uuid + "';");
    }

    public static void activate(UUID uuid) {
        if (active.contains(uuid)) {
            return;
        }
        active.add(uuid);
        DB.executeUpdateAsync("INSERT INTO `ts_boats_active` (`uuid`) VALUES('" + uuid +  "');");
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
            case DodgeRam -> {
                return new DodgeRam(player, location);
            }
            case MazdaRX7FC3S -> {
                return new MazdaRX7FC3S(player, location);
            }
            default -> {
                return new TechnosVolvo(player, location);
            }
        }
    }

    public static ItemStack getBoatItem(BoatType boatType, String name){
        switch (boatType) {
            case MazdaRX7 -> {
                return getBoatItem(MazdaRX7.customModelData, name);
            }
            case BasicOrange -> {
                return getBoatItem(BasicOrange.customModelData, name);
            }
            case AdamsMatrix -> {
                return getBoatItem(AdamsMatrix.customModelData, name);
            }
            case RenosLegacy -> {
                return getBoatItem(RenosLegacy.customModelData, name);
            }
            case RallySubaru -> {
                return getBoatItem(RallySubaru.customModelData, name);
            }
            case DodgeRam -> {
                return getBoatItem(DodgeRam.customModelData, name);
            }
            case MazdaRX7FC3S -> {
                return getBoatItem(MazdaRX7FC3S.customModelData, name);
            }
            default -> {
                return getBoatItem(TechnosVolvo.customModelData, name);
            }
        }
    }

    public static ItemStack getBoatItem(int customModelData, String name) {
        ItemStack i = new ItemStack(Material.STICK, 1);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(name);
        im.setCustomModelData(customModelData);
        i.setItemMeta(im);
        return i;
    }


}
