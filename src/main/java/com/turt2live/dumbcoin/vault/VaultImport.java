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
        for (Economy economy : economies) {
            if (economy.getName().equalsIgnoreCase(pluginName)) {
                doImport(economy);
                break;
            }
        }
    }

    public void doImport() {
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
                        plugin.getBalanceManager().set(players[i].getName(), balance);
                        int percent = (int) ((((double) i) / ((double) players.length)) * 100);
                        if (percent % 10 == 0 && percent != lastPercent) {
                            plugin.getLogger().info("[Plugin: " + economy.getName() + "]" + i + "/" + players.length + " imported. (" + percent + "%)");
                            lastPercent = percent;
                        }
                    }
                }
            });
        }
    }
}
