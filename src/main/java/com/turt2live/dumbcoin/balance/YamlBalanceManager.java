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

package com.turt2live.dumbcoin.balance;

import com.turt2live.dumbcoin.DumbCoin;
import com.turt2live.hurtle.uuid.UUIDServiceProvider;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class YamlBalanceManager extends BalanceManager {

    private File file;
    private YamlConfiguration yaml;

    public YamlBalanceManager(DumbCoin plugin) {
        super(plugin);
        file = new File(plugin.getDataFolder(), "balances.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deposit(UUID player, double amount) {
        yaml.set(player == null ? "CONSOLE" : player.toString().replace("-", ""), getBalance(player) + amount);
    }

    @Override
    public void withdraw(UUID player, double amount) {
        deposit(player, -amount);
    }

    @Override
    public double getBalance(UUID player) {
        if (yaml.get(player == null ? "CONSOLE" : player.toString().replace("-", "")) != null) {
            return yaml.getDouble(player == null ? "CONSOLE" : player.toString().replace("-", ""));
        }
        return plugin.getConfig().getDouble("start-balance", 10);
    }

    @Override
    public double getBalanceNoStart(UUID player) {
        if (yaml.get(player == null ? "CONSOLE" : player.toString().replace("-", "")) != null) {
            return yaml.getDouble(player == null ? "CONSOLE" : player.toString().replace("-", ""));
        }
        return 0;
    }

    @Override
    public void set(UUID player, double amount) {
        yaml.set(player == null ? "CONSOLE" : player.toString().replace("-", ""), amount);
    }

    @Override
    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<UUID, Double> getBalances() {
        Map<UUID, Double> balances = new HashMap<UUID, Double>();
        Set<String> keys = yaml.getKeys(false);
        if (keys != null) {
            for (String s : keys) {
                if (s.length() < 32) continue;
                UUID uid = UUID.fromString(UUIDServiceProvider.insertDashes(s));
                balances.put(uid, getBalance(uid));
            }
        }
        return balances;
    }

    @Override
    public Map<String, Double> getLegacyBalances() {
        Map<String, Double> balances = new HashMap<String, Double>();
        Set<String> keys = yaml.getKeys(false);
        if (keys != null) {
            for (String s : keys) {
                if (s.length() > 16) continue;
                balances.put(s, yaml.getDouble(s.toLowerCase(), plugin.getConfig().getDouble("start-balance", 10)));
            }
        }
        return balances;
    }

    @Override
    public void removeLegacyBalance(String name) {
        if (name == null) return;
        yaml.set(name, null);
    }

}
