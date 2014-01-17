package com.turt2live.dumbcoin.balance;

import com.turt2live.commonsense.data.MySQL;
import com.turt2live.dumbcoin.DumbCoin;
import com.turt2live.dumbcoin.Queries;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
        createTable();
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
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.GET_BALANCE));
        try {
            statement.setString(1, player);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet != null && resultSet.getFetchSize() > 0) {
                return resultSet.getDouble("Balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void set(String player, double amount) {
        update(player, amount, true);
    }

    @Override
    public Map<String, Double> getBalances() {
        Map<String, Double> map = new HashMap<String, Double>();
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.GET_ALL_BALANCES));
        try {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet != null && resultSet.getFetchSize() > 0) {
                do {
                    map.put(resultSet.getString("Username"), resultSet.getDouble("Balance"));
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void update(String player, double amount, boolean isSet) {
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(isSet ? Queries.Query.UPDATE_BALANCE_SET : Queries.Query.UPDATE_BALANCE_MOD));
        try {
            statement.setString(1, player);
            statement.setDouble(2, amount);
            statement.setDouble(3, amount);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.CREATE_TABLE));
        try {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        mysql.disconnect();
    }
}
