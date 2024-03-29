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

import com.turt2live.dumbcoin.balance.BalanceManager;
import com.turt2live.hurtle.uuid.UUIDUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

class UUIDChangeover implements Runnable {

    private CommandSender sender;
    private DumbCoin plugin = DumbCoin.p;

    public UUIDChangeover(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void run() {
        BalanceManager manager = plugin.getBalanceManager();
        Map<String, Double> legacy = manager.getLegacyBalances();
        plugin.getLogger().info("UUID Changeover started");
        if (legacy != null) {
            File backupFile = new File(plugin.getDataFolder(), "backup_balances.yml");
            int c = 1;
            while (backupFile.exists()) {
                backupFile = new File(plugin.getDataFolder(), "backup_balances" + c + ".yml");
                c++;
            }
            try {
                backupFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            YamlConfiguration backup = YamlConfiguration.loadConfiguration(backupFile);

            for (Map.Entry<String, Double> record : legacy.entrySet()) {
                plugin.getLogger().info("Converting " + record.getKey() + " (" + record.getValue() + ")...");
                UUID uuid = UUIDUtils.getUUID(record.getKey());
                if (uuid == null) {
                    plugin.getLogger().warning("Warning! No UUID found for account " + record.getKey() + "!");
                    continue;
                }
                double current = manager.getBalanceNoStart(uuid);
                current += record.getValue();
                manager.set(uuid, current);
                manager.removeLegacyBalance(record.getKey());
                backup.set(record.getKey(), record.getValue());
            }

            try {
                backup.save(backupFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        plugin.getLogger().info((legacy == null ? 0 : legacy.size()) + " record(s) converted!");
        plugin.sendMessage(sender, ChatColor.GREEN + "Legacy changeover complete!");
    }
}
