package com.turt2live.dumbcoin.balance;

import com.turt2live.dumbcoin.DumbCoin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public void deposit(String player, double amount) {
        yaml.set(player.toLowerCase(), getBalance(player) + amount);
        onChange(player, amount);
    }

    @Override
    public void withdraw(String player, double amount) {
        deposit(player, -amount);
        onChange(player, -amount);
    }

    @Override
    public double getBalance(String player) {
        if (yaml.get(player.toLowerCase()) != null) {
            return yaml.getDouble(player.toLowerCase());
        }
        return plugin.getConfig().getDouble("start-balance", 10);
    }

    @Override
    public void set(String player, double amount) {
        double difference = -(getBalance(player) - amount); // 10, set to 7, = -3.  7, set to 10, = 3
        yaml.set(player.toLowerCase(), amount);
        onChange(player, difference);
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
    public Map<String, Double> getBalances() {
        Map<String, Double> balances = new HashMap<String, Double>();
        Set<String> keys = yaml.getKeys(false);
        if (keys != null) {
            for (String s : keys) {
                balances.put(s, getBalance(s));
            }
        }
        return balances;
    }

}
