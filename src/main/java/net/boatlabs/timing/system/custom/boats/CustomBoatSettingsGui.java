package net.boatlabs.timing.system.custom.boats;

import me.makkuusen.timing.system.ApiUtilities;
import me.makkuusen.timing.system.ItemBuilder;
import me.makkuusen.timing.system.TPlayer;
import me.makkuusen.timing.system.api.TimingSystemAPI;
import me.makkuusen.timing.system.gui.BaseGui;
import me.makkuusen.timing.system.gui.ButtonUtilities;
import me.makkuusen.timing.system.gui.GuiButton;
import me.makkuusen.timing.system.gui.SettingsGui;
import org.bukkit.Material;
import org.bukkit.entity.Boat;

public class CustomBoatSettingsGui extends BaseGui {

    public CustomBoatSettingsGui(TPlayer tPlayer) {
        super("§2§lSettings", 3);
        setButtons(tPlayer);
    }

    private void setButtons(TPlayer tPlayer){
        int count = 0;
        for (Material boat : ApiUtilities.getBoatMaterials()) {
            if (boat != Material.OAK_BOAT) {
                setItem(getBoatTypeButton(tPlayer, boat), count);
                count++;
            }
        }
        for (BoatType bt : BoatType.values()) {
            setItem(getCustomBoatButton(tPlayer, bt), count);
            count++;
        }
        setItem(ButtonUtilities.getReturnToSettingsButton(tPlayer), 26);
    }

    private GuiButton getBoatTypeButton(TPlayer tPlayer, Material boatType) {
        var button = new GuiButton(new ItemBuilder(boatType).setName("§eBoat").build());
        button.setAction(() -> {
            tPlayer.setBoat(ApiUtilities.getBoatType(boatType));
            tPlayer.setChestBoat(ApiUtilities.isChestBoat(boatType));
            if (tPlayer.getPlayer().getVehicle() instanceof Boat boat) {
                boat.setBoatType(tPlayer.getBoat());
            }
            if (tPlayer.isSound()) {
                ButtonUtilities.playConfirm(tPlayer.getPlayer());
            }
            if (TimingSystemCustomBoats.hasPlayerBoat(tPlayer.getUniqueId())) {
                TimingSystemCustomBoats.removePlayerBoat(tPlayer.getUniqueId());
            }
            new SettingsGui(tPlayer).show(tPlayer.getPlayer());
        });
        return button;
    }

    public GuiButton getCustomBoatButton(TPlayer tPlayer, BoatType boatType){
        var button = new GuiButton(TimingSystemCustomBoats.getBoatItem(boatType,"§e" + boatType.name()));
        button.setAction(()-> {
            TimingSystemCustomBoats.setPlayerBoat(tPlayer.getPlayer().getUniqueId(), boatType);
            new SettingsGui(tPlayer).show(tPlayer.getPlayer());
        });
        return button;
    }

}
