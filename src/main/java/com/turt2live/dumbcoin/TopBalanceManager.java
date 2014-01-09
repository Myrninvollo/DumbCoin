package com.turt2live.dumbcoin;

import com.turt2live.dumbcoin.util.BalanceQuicksort;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopBalanceManager {

    public static final class PlayerBalance implements ConfigurationSerializable, Cloneable {
        private String playerName;
        private double balance;
        protected int rank;

        public PlayerBalance(String name, double balance, int rank) {
            this.playerName = name;
            this.balance = balance;
            this.rank = rank;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getBalance() {
            return balance;
        }

        public int getRank() {
            return rank;
        }

        @Override
        public PlayerBalance clone() {
            return new PlayerBalance(playerName, balance, rank);
        }

        public PlayerBalance clone(int newRank) {
            return new PlayerBalance(playerName, balance, newRank);
        }

        @Override
        public String toString() {
            return "#" + rank + " " + playerName + " " + balance;
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("playerName", playerName);
            map.put("balance", balance);
            map.put("rank", rank);
            return map;
        }

        public static PlayerBalance deserialize(Map<String, Object> map) {
            if (map.containsKey("playerName") && map.get("playerName") instanceof String) {
                String playerName = (String) map.get("playerName");
                if (map.containsKey("balance") && map.get("balance") instanceof Double) {
                    double balance = (Double) map.get("balance");
                    if (map.containsKey("rank") && map.get("rank") instanceof Integer) {
                        return new PlayerBalance(playerName, balance, (Integer) map.get("rank"));
                    }
                }
            }
            return null;
        }
    }

    private Map<Integer, PlayerBalance> rankings = new ConcurrentHashMap<Integer, PlayerBalance>();
    private long lastSort = 0L;
    private BalanceQuicksort sorter = new BalanceQuicksort();

    // Inclusive
    public void getBalances(final int minRange, final int maxRange, final FutureBalances future) {
        if (System.currentTimeMillis() - lastSort > (1000 * 60 * DumbCoin.p.getConfig().getInt("advanced.sort-every-minutes"))) {
            DumbCoin.p.getServer().getScheduler().runTaskAsynchronously(DumbCoin.p, new Runnable() {
                @Override
                public void run() {
                    resort();
                    future.accept(getRange(minRange, maxRange), minRange, maxRange);
                }
            });
            return;
        }
        future.accept(getRange(minRange, maxRange), minRange, maxRange);
    }

    private List<PlayerBalance> getRange(int minRange, int maxRange) {
        List<PlayerBalance> balances = new ArrayList<PlayerBalance>();
        for (int i = minRange; i <= maxRange; i++) {
            PlayerBalance balance = rankings.get(i);
            if (balance != null) {
                balance = balance.clone(i);
                balances.add(balance);
            }
        }
        return balances;
    }

    private void resort() {
        List<PlayerBalance> allBalances = new ArrayList<PlayerBalance>();
        Map<String, Double> balances = DumbCoin.p.getBalanceManager().getBalances();
        for (String s : balances.keySet()) {
            PlayerBalance playerBalance = new PlayerBalance(s, balances.get(s), 0);
            allBalances.add(playerBalance);
        }
        sorter.sort(allBalances);
        rankings.clear();
        for (PlayerBalance balance : allBalances) {
            rankings.put(balance.getRank(), balance);
        }
        lastSort = System.currentTimeMillis();
    }

}
