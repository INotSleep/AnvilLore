package me.inotsleep.anvillore;

import net.kyori.adventure.text.Component;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listeners implements Listener {
    Config config = AnvilLore.config;

    private long calcExpirience(int level) {
        if (level <=16) {
            return (long) level * level + level* 6L;
        } else if (level <= 31) {
            return (long) (2.5 * level*level - 40.5 * level + 360);
        } else {
            return (long) (4.5 * level*level - 162.5 * level + 2220);
        }
    }

    private List<Number> calcLevels(int exp) {
        int lvl;
        if (exp <=352) {
            lvl = (int) Math.floor(Math.sqrt(exp+9)-3);
        } else if (exp <= 1507) {
            lvl = (int) Math.floor(8.1d + Math.sqrt(0.4d*(exp-195.975d)));
        } else {
            lvl = (int) Math.floor((325d/18d)+Math.sqrt((2d/9d)*(exp-(54215d/72d))));
        }

        return Arrays.asList(lvl, (exp-calcExpirience(lvl))/(calcExpirience(lvl+1)/calcExpirience(lvl)));
    }

    private int getExp(int lvl, float exp) {
        return (int) (exp*(calcExpirience(lvl+1)-calcExpirience(lvl)));
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        Player player = (Player) event.getView().getPlayer();
        String prefix = config.getString("settings.addLorePrefix");
        if (config.config.getBoolean("settings.ignoreCase", false)) prefix = prefix.toLowerCase();

        if (inventory.getRenameText() == null) return;
        if (inventory.getFirstItem() == null || inventory.getResult() == null) return;
        if (!((config.config.getBoolean("settings.ignoreCase") && inventory.getRenameText().toLowerCase().startsWith(prefix)) || inventory.getRenameText().startsWith(prefix))) return;
        if (!player.hasPermission(config.getString("permissions.use"))) return;

        String lore = Pattern.compile(prefix, Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(inventory.getRenameText()).replaceAll(Matcher.quoteReplacement("")); //.replace(prefix, "");
        if (player.hasPermission(config.getString("permissions.formatting"))) lore = ChatColor.translateAlternateColorCodes('&', "&f"+lore);
        else if (config.config.getBoolean("settings.removeFormattingWhileNoPerms")) lore = lore.replaceAll("&[0-9abcdefklmno]", "");

        ItemStack firstItem = inventory.getFirstItem();
        ItemStack resultItem = inventory.getResult();

        PersistentDataContainer container = firstItem.getItemMeta().getPersistentDataContainer();
        Integer lineCountRaw = container.get(new NamespacedKey(AnvilLore.getInstance(), "lineCount"), PersistentDataType.INTEGER);
        int lineCount = lineCountRaw == null ? 0 : lineCountRaw;

        if (config.config.getBoolean("settings.allowMultiLine", true)) {
            if (!player.hasPermission(config.getString("permissions.limit.bypass"))) {
                String permPrefix = config.getString("permissions.limit.prefix");
                for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
                    if (!info.getPermission().startsWith(permPrefix)) continue;
                    String s = info.getPermission().replace(permPrefix, "");
                    if (!s.matches("[0-9]+")) continue;
                    if (Integer.parseInt(s) >= lineCount) return;
                }
            }
        }
        ItemMeta meta = resultItem.getItemMeta();
        PersistentDataContainer resultContainer = meta.getPersistentDataContainer();

        Expression expression = new ExpressionBuilder(config.getString("price.expression").replaceAll("lines", String.valueOf(lineCount)).replaceAll("symbols", String.valueOf(lore.length())).replaceAll("base", String.valueOf(config.config.getDouble("price.baseValue")))).build();
        double price = expression.evaluate();

        resultContainer.set(new NamespacedKey(AnvilLore.getInstance(), "changing"), PersistentDataType.BYTE, (byte) 1);
        resultContainer.set(new NamespacedKey(AnvilLore.getInstance(), "lineCount"), PersistentDataType.INTEGER, lineCount+1);
        resultContainer.set(new NamespacedKey(AnvilLore.getInstance(), "price"), PersistentDataType.DOUBLE, price);

        boolean isEnough = false;

        switch (config.priceType) {
            case ECONOMY: {
                isEnough = AnvilLore.economy.getBalance(player) >= price;
                break;

            }
            case LEVELS: {
                isEnough = player.getLevel() >= price+inventory.getRepairCost();
                break;
            }
            case POINTS: {
                if (player.getLevel()-inventory.getRepairCost() <0) break;
                isEnough = calcExpirience(player.getLevel()-inventory.getRepairCost()) + player.getExp() >= price;
                break;
            }
        }

        meta.displayName(firstItem.getItemMeta().displayName());
        List<Component> itemLore = meta.lore();

        if (itemLore == null) itemLore = Arrays.asList(Component.text(lore), Component.empty(), Component.text(config.format(config.getString(isEnough || !config.config.getBoolean("settings.messageNoFunds") ? "messages.price" : "messages.notEnoughMoney"), String.valueOf(price))));
        else itemLore.addAll(Arrays.asList(Component.text(lore), Component.empty(), Component.text(config.format(config.getString(isEnough || !config.config.getBoolean("settings.messageNoFunds") ? "messages.price" : "messages.notEnoughMoney"), String.valueOf(price)))));
        meta.lore(itemLore);
        resultItem.setItemMeta(meta);

        event.setResult(resultItem);
    }

    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (event.getInventory() instanceof AnvilInventory && event.getRawSlot() == 2) {
            AnvilInventory inventory = (AnvilInventory) event.getInventory();
            ItemStack result = inventory.getResult();
            if (result == null) return;

            ItemMeta meta = result.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            Byte data = container.get(new NamespacedKey(AnvilLore.getInstance(), "changing"), PersistentDataType.BYTE);
            Double price = container.get(new NamespacedKey(AnvilLore.getInstance(), "price"), PersistentDataType.DOUBLE);

            List<Component> lore = meta.lore();
            if (data == null || data != 1 || lore == null || price == null) return;

            Player player = (Player) event.getView().getPlayer();

            switch (config.priceType) {
                case ECONOMY: {
                    if (AnvilLore.economy.getBalance(player) >= price) AnvilLore.economy.withdrawPlayer(player, price);
                    else {
                        event.setCancelled(true);
                        return;
                    }
                    break;

                }
                case LEVELS: {
                    if (player.getLevel() >= price+inventory.getRepairCost())  {
                        player.setLevel((int)(player.getLevel() - price - inventory.getRepairCost()));
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                    break;
                }
                case POINTS: {
                    if (player.getLevel()-inventory.getRepairCost() <0) {
                        event.setCancelled(true);
                        return;
                    };
                    if (calcExpirience(player.getLevel()-inventory.getRepairCost()) + getExp(player.getLevel(), player.getExp()) >= price) {
                        List<Number> list = calcLevels((int) (calcExpirience(player.getLevel())+getExp(player.getLevel(), player.getExp())-price));
                        player.setLevel((Integer) list.get(0));
                        player.setExp((Float) list.get(1));
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                    break;
                }
            }

            lore = lore.subList(0, lore.size()-2);
            meta.lore(lore);
            container.set(new NamespacedKey(AnvilLore.getInstance(), "changing"), PersistentDataType.BYTE, (byte) 0);
            result.setItemMeta(meta);
            inventory.setResult(result);
        }
    }
}
