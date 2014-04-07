package com.turt2live.dumbcoin.balance;

import com.turt2live.dumbcoin.DumbCoin;

import java.util.Map;
import java.util.UUID;

public abstract class BalanceManager {

    protected DumbCoin plugin;

    public BalanceManager(DumbCoin plugin) {
        this.plugin = plugin;
    }

    public abstract void save();

    public abstract void deposit(UUID player, double amount);

    public abstract void withdraw(UUID player, double amount);

    public abstract double getBalance(UUID player);

    public abstract void set(UUID player, double amount);

    public abstract Map<UUID, Double> getBalances();

    public abstract Map<String, Double> getLegacyBalances();

    public abstract void removeLegacyBalance(String name);

    public boolean hasEnough(UUID player, double amount) {
        return getBalance(player) >= amount;
    }

    public void pay(UUID payer, UUID payee, double amount) {
        withdraw(payer, amount);
        deposit(payee, amount);
    }

}
