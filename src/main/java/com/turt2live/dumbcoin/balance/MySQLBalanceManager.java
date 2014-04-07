package com.turt2live.dumbcoin.balance;

import com.turt2live.commonsense.data.MySQL;
import com.turt2live.dumbcoin.DumbCoin;
import com.turt2live.dumbcoin.Queries;
import com.turt2live.hurtle.uuid.UUIDServiceProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public void deposit(UUID player, double amount) {
        update(player, amount, false);
    }

    @Override
    public void withdraw(UUID player, double amount) {
        update(player, -amount, false);
    }

    @Override
    public double getBalance(UUID player) {
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.GET_BALANCE));
        try {
            statement.setString(1, player == null ? "CONSOLE" : player.toString().replace("-", ""));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("Balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plugin.getConfig().getDouble("start-balance", 10);
    }

    @Override
    public void set(UUID player, double amount) {
        update(player, amount, true);
    }

    @Override
    public Map<UUID, Double> getBalances() {
        Map<UUID, Double> map = new HashMap<UUID, Double>();
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.GET_ALL_BALANCES));
        try {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                do {
                    String name = resultSet.getString("Username");
                    if (name.length() < 32) {
                        continue;
                    }
                    map.put(UUID.fromString(UUIDServiceProvider.insertDashes(name)), resultSet.getDouble("Balance"));
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Double> getLegacyBalances() {
        Map<String, Double> map = new HashMap<String, Double>();
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.GET_ALL_BALANCES));
        try {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                do {
                    String name = resultSet.getString("Username");
                    if (name.length() > 16) {
                        continue;
                    }
                    map.put(name, resultSet.getDouble("Balance"));
                } while (resultSet.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void removeLegacyBalance(String player) {
        if (player == null) return;
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.REMOVE_LEGACY));
        try {
            statement.setString(1, player);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void update(UUID player, double amount, boolean isSet) {
        if (!mysql.isConnected()) mysql.connect();
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(isSet ? Queries.Query.UPDATE_BALANCE_SET : Queries.Query.UPDATE_BALANCE_MOD));
        try {
            statement.setString(1, player == null ? "CONSOLE" : player.toString().replace("-", ""));
            statement.setDouble(2, amount);
            statement.setDouble(3, amount);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        if (!mysql.isConnected()) mysql.connect();
        PreparedStatement statement = mysql.getPreparedStatement(queries.getQuery(Queries.Query.CREATE_TABLE));
        try {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        mysql.disconnect();
    }
}
