package com.turt2live.dumbcoin.balance;

import com.turt2live.dumbcoin.DumbCoin;

import java.util.Map;

public abstract class BalanceManager {

    protected DumbCoin plugin;

    public BalanceManager(DumbCoin plugin) {
        this.plugin = plugin;
    }

    public abstract void deposit(String player, double amount);

    public abstract void withdraw(String player, double amount);

    public abstract double getBalance(String player);

    public abstract void set(String player, double amount);

    public abstract void save();

    public abstract Map<String, Double> getBalances();

    public boolean hasEnough(String player, double amount) {
        return getBalance(player) >= amount;
    }

    public void pay(String payer, String payee, double amount) {
        withdraw(payer, amount);
        deposit(payee, amount);
    }

}
