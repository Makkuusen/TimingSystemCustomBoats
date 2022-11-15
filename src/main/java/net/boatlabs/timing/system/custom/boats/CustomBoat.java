package net.boatlabs.timing.system.custom.boats;

import me.makkuusen.timing.system.TimingSystem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public abstract class CustomBoat {
    Player player;
    Location spawnLocation;
    Villager villager = null;
    Boat boat = null;
    int customModelData;

    public CustomBoat(Player player, Location location, int customModelData){
        this.player = player;
        this.spawnLocation = location;
        this.customModelData = customModelData;
    }

    public Boat getBoat(){
        return boat;
    }

    public void spawnBoat() {
        this.villager = spawnVillager(spawnLocation, customModelData);
        this.boat = spawnCustomBoat(spawnLocation);
        boat.addPassenger(player);
        boat.addPassenger(villager);
    }

    public void remove() {
        villager.remove();
        boat.remove();
    }

    public ItemStack getBoatItem() {
        return null;
    }

    private static org.bukkit.entity.Boat spawnCustomBoat(Location location) {
        if (!location.isWorldLoaded()) {
            return null;
        }
        org.bukkit.entity.Boat boat = (Boat) location.getWorld().spawnEntity(location, EntityType.BOAT);
        boat.setMetadata("customboat", new FixedMetadataValue(TimingSystem.getPlugin(), null));
        return boat;
    }

    private static Villager spawnVillager(Location location, int customModelData) {
        if (!location.isWorldLoaded()) {
            return null;
        }
        ItemStack i = new ItemStack(Material.STICK, 1);
        ItemMeta im = i.getItemMeta();
        im.setCustomModelData(customModelData);
        i.setItemMeta(im);
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setInvisible(true);
        villager.setInvulnerable(true);
        villager.setAI(false);
        villager.setSilent(true);
        villager.getEquipment().setItemInMainHand(i);
        villager.setInvulnerable(true);
        return villager;
    }

}
