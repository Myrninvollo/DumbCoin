package com.turt2live.dumbcoin;

import com.turt2live.commonsense.data.MySQL;
import com.turt2live.commonsense.data.NoDriverException;
import com.turt2live.dumbcoin.balance.BalanceManager;
import com.turt2live.dumbcoin.balance.MySQLBalanceManager;
import com.turt2live.dumbcoin.balance.YamlBalanceManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

class BalanceConverter implements Runnable {

    public static enum Format {
        MYSQL, YAML;

        public static Format fromName(String s) {
            if (s == null) return null;
            for (Format f : values()) {
                if (f.name().equalsIgnoreCase(s)) {
                    return f;
                }
            }
            return null;
        }
    }

    private Format from, to;
    private CommandSender sender;
    private DumbCoin plugin = DumbCoin.p;

    public BalanceConverter(Format from, Format to, CommandSender sender) {
        this.from = from;
        this.to = to;
        this.sender = sender;
    }

    public void run() {
        BalanceManager from, to;
        if (this.from == Format.YAML) from = new YamlBalanceManager(plugin);
        else {
            try {
                MySQL sql = new MySQL(plugin.getConfig().getString("storage.mysql.hostname", "localhost"),
                        plugin.getConfig().getInt("storage.mysql.port", 3306),
                        plugin.getConfig().getString("storage.mysql.username", "user"),
                        plugin.getConfig().getString("storage.mysql.password", "pass"),
                        plugin.getConfig().getString("storage.mysql.database", "DumbCoin"));
                if (sql.connect() == MySQL.ConnectionStatus.CONNECTED && sql.isConnected()) {
                    from = new MySQLBalanceManager(plugin, sql, new Queries(plugin, "mysql-data.sql"));
                } else {
                    plugin.sendMessage(sender, ChatColor.RED + "Failed to connect to MySQL");
                    return;
                }
            } catch (NoDriverException e) {
                plugin.sendMessage(sender, ChatColor.RED + "Failed to connect to MySQL");
                return;
            }
        }

        if (this.to == Format.YAML) to = new YamlBalanceManager(plugin);
        else {
            try {
                MySQL sql = new MySQL(plugin.getConfig().getString("storage.mysql.hostname", "localhost"),
                        plugin.getConfig().getInt("storage.mysql.port", 3306),
                        plugin.getConfig().getString("storage.mysql.username", "user"),
                        plugin.getConfig().getString("storage.mysql.password", "pass"),
                        plugin.getConfig().getString("storage.mysql.database", "DumbCoin"));
                if (sql.connect() == MySQL.ConnectionStatus.CONNECTED && sql.isConnected()) {
                    to = new MySQLBalanceManager(plugin, sql, new Queries(plugin, "mysql-data.sql"));
                } else {
                    plugin.sendMessage(sender, ChatColor.RED + "Failed to connect to MySQL");
                    return;
                }
            } catch (NoDriverException e) {
                plugin.sendMessage(sender, ChatColor.RED + "Failed to connect to MySQL");
                return;
            }
        }

        Map<UUID, Double> balances = from.getBalances();
        for (Map.Entry<UUID, Double> balance : balances.entrySet()) {
            plugin.getLogger().info("Converting " + balance.getKey().toString().replace("-", "") + " (" + balance.getValue() + ")...");
            to.set(balance.getKey(), to.getBalanceNoStart(balance.getKey()) + balance.getValue());
        }

        plugin.sendMessage(sender, ChatColor.GREEN + "Conversion complete. Old data was not removed.");
    }

}
