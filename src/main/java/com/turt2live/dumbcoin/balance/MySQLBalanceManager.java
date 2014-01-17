package com.turt2live.dumbcoin.balance;

import com.turt2live.commonsense.data.MySQL;
import com.turt2live.dumbcoin.DumbCoin;
import com.turt2live.dumbcoin.Queries;

import java.util.Map;

/**
 * Represents a balance manager which uses MySQL as a data store
 *
 * @author turt2live
 */
public class MySQLBalanceManager extends BalanceManager {

    private MySQL mysql;
    private Queries queries;

    /**
     * Creates a new MySQL balance manager
     *
     * @param plugin     the DumbCoin plugin instance
     * @param connection the MySQL connection to use
     * @param queries    the queries instance to use
     */
    public MySQLBalanceManager(DumbCoin plugin, MySQL connection, Queries queries) {
        super(plugin);
        if (connection == null || queries == null) throw new IllegalArgumentException();
        this.mysql = connection;
        this.queries = queries;
    }

    @Override
    public void deposit(String player, double amount) {
        update(player, amount, false);
    }

    @Override
    public void withdraw(String player, double amount) {
        update(player, -amount, false);
    }

    @Override
    public double getBalance(String player) {
        // TODO
        return 0;
    }

    @Override
    public void set(String player, double amount) {
        // TODO
    }

    @Override
    public Map<String, Double> getBalances() {
        // TODO
        return null;
    }

    private void update(String player, double amount, boolean isSet) {
        // TODO
    }

    private void createTable() {
        // TODO
    }

    @Override
    public void save() {
        mysql.disconnect();
    }
}
