/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

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
