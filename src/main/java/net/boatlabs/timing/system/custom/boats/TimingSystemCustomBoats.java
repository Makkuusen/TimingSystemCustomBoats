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
import net.boatlabs.timing.system.custom.boats.types.TechnosVolvo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TimingSystemCustomBoats extends JavaPlugin {

    public static HashMap<UUID, BoatType> playerBoats = new HashMap<>();
    public static HashMap<UUID, CustomBoat> boats = new HashMap<>();

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

    public boolean createTables() {
        try {
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_boats` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `boat` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
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
                TimingSystemCustomBoats.playerBoats.put(uuid, type);
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
}
