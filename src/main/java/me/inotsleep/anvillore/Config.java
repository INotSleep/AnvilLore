package me.inotsleep.anvillore;

import me.inotsleep.utils.AbstractConfig;
import me.inotsleep.utils.AbstractPlugin;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Config extends AbstractConfig {
    PriceType priceType;
    public Config(AbstractPlugin plugin) {
        super(plugin, "config.yml", false);
    }

    @Override
    public void addDefaults() {
        addDefault("price.type", "ECONOMY");
        addDefault("price.baseValue", 100d);
        addDefault("price.expression", "base+(symbols*1.3)+base*lines");

        addDefault("permissions.formatting", "anvillore.formatting");
        addDefault("permissions.use", "anvillore.use");
        addDefault("permissions.limit.bypass", "anvillore.limit.bypass");
        addDefault("permissions.limit.prefix", "anvillore.limit.");

        addDefault("settings.removeFormattingWhileNoPerms", true);
        addDefault("settings.allowMultiLine", true);
        addDefault("settings.addLorePrefix", "lore:");
        addDefault("settings.messageNoFunds", true);
        addDefault("settings.allowInCombine", true);
        addDefault("settings.ignoreCase", true);

        addDefault("messages.price", "&aPrice: {0}$");
        addDefault("messages.notEnoughMoney", "&4Not enought money. Need: {0}$");
    }

    @Override
    public void doReloadConfig() {
        switch(getString("price.type")) {
            case "ECONOMY": {
                if (!AnvilLore.economyEnabled) {
                    AnvilLore.logger.log(Level.SEVERE, "'ECONOMY' price type is not supported while Vault economy is not found. Disabling...");
                    Bukkit.getPluginManager().disablePlugin(AnvilLore.getInstance());
                    return;
                }
                priceType = PriceType.ECONOMY;
                break;
            }
            case "LEVELS": {
                priceType = PriceType.LEVELS;
                break;
            }
            case "POINTS": {
                priceType = PriceType.POINTS;
                break;
            }
            default: {
                AnvilLore.logger.log(Level.SEVERE, "Invalid price type in config. Supported: 'ECONOMY', 'LEVELS', 'POINTS'. Disabling...");
                Bukkit.getPluginManager().disablePlugin(AnvilLore.getInstance());
            }
        }
    }

    @Override
    public void doSave() {

    }

    enum PriceType {
        ECONOMY, LEVELS, POINTS
    }

}
