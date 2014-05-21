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

package com.turt2live.dumbcoin.vault;

import com.turt2live.dumbcoin.DumbCoin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VaultImport {

    private List<Economy> economies = new ArrayList<Economy>();

    public VaultImport() {
        DumbCoin plugin = DumbCoin.p;
        Collection<RegisteredServiceProvider<Economy>> providers = plugin.getServer().getServicesManager().getRegistrations(Economy.class);
        if (providers == null) {
            return;
        }
        for (RegisteredServiceProvider<Economy> econProvider : providers) {
            Economy provider = econProvider.getProvider();
            if (!(provider instanceof Economy_DumbCoin)) {
                economies.add(provider);
            }
        }
    }

    public void doImport(String pluginName) {
        if (economies.size() <= 0) {
            DumbCoin.p.getLogger().warning("No plugins to import!");
            return;
        }
        for (Economy economy : economies) {
            if (economy.getName().equalsIgnoreCase(pluginName)) {
                doImport(economy);
                return;
            }
        }
        if (economies.size() <= 0) DumbCoin.p.getLogger().warning("Cannot find plugin '" + pluginName + "' to import.");
    }

    public void doImport() {
        if (economies.size() <= 0) {
            DumbCoin.p.getLogger().warning("No plugins to import!");
            return;
        }
        for (Economy economy : economies) {
            doImport(economy);
        }
    }

    public boolean canImport(String pluginName) {
        for (Economy economy : economies) {
            if (economy.getName().equalsIgnoreCase(pluginName)) return true;
        }
        return false;
    }

    public void doImport(final Economy economy) {
        if (economy != null) {
            final DumbCoin plugin = DumbCoin.p;
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getLogger().info("[Plugin: " + economy.getName() + "] Iterating over all offline players...");
                    OfflinePlayer[] players = plugin.getServer().getOfflinePlayers();
                    int lastPercent = -1;
                    for (int i = 0; i < players.length; i++) {
                        double balance = economy.getBalance(players[i].getName());
                        plugin.getBalanceManager().set(players[i].getUniqueId(), balance);
                        int percent = (int) ((((double) i) / ((double) players.length)) * 100);
                        if (percent % 10 == 0 && percent != lastPercent) {
                            plugin.getLogger().info("[Plugin: " + economy.getName() + "]" + i + "/" + players.length + " imported. (" + percent + "%)");
                            lastPercent = percent;
                        }
                    }
                    plugin.getLogger().info("[Plugin: " + economy.getName() + "] Import complete!");
                }
            });
        }
    }
}
