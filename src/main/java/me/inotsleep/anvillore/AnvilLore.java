package me.inotsleep.anvillore;

import me.inotsleep.utils.AbstractPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;
public final class AnvilLore extends AbstractPlugin {

    public static Economy economy;
    public static boolean economyEnabled;
    public static Logger logger;
    public static Config config;

    @Override
    public void doDisable() {

    }

    @Override
    public void doEnable() {
        logger = getLogger();

        logger.info("================================");
        logger.info("= Anvil Lore      by INotSleep =");
        logger.info("================================");

        economyEnabled = setupEconomy();

        if (!economyEnabled) {
            logger.warning("Vault Economy not found.");
            logger.warning("Price type ECONOMY is not supported!");
        }

        config = new Config(this);
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }
}
