package com.turt2live.dumbcoin.vault;

import com.turt2live.dumbcoin.DumbCoin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.List;

public class Economy_DumbCoin implements Economy {

    private DumbCoin plugin;

    public Economy_DumbCoin(DumbCoin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public int fractionalDigits() {
        return plugin.getDecimalPlaces();
    }

    @Override
    public String format(double v) {
        return plugin.format(v);
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getConfig().getString("advanced.plural", "Dollars");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfig().getString("advanced.singular", "Dollar");
    }

    @Override
    public double getBalance(String playerName) {
        return plugin.getBalanceManager().getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, double v) {
        return plugin.getBalanceManager().hasEnough(playerName, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double v) {
        plugin.getBalanceManager().withdraw(playerName, v);
        return new EconomyResponse(v, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double v) {
        plugin.getBalanceManager().deposit(playerName, v);
        return new EconomyResponse(v, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null);
    }

    // ======= PER-WORLD STUFF (redirects) =======

    @Override
    public boolean hasAccount(String playerName, String world) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, String world, double v) {
        return has(playerName, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String world, double v) {
        return withdrawPlayer(playerName, v);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String world, double v) {
        return depositPlayer(playerName, v);
    }

    // ======= SHIT THAT DOESN'T MATTER =======

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String world) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName) {
        return true; // All accounts have balances
    }

    // ======= BANKS =======

    @Override
    public boolean hasBankSupport() {
        return false; // TODO : Maybe bank support?
    }

    @Override
    public EconomyResponse createBank(String playerName, String world) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public EconomyResponse deleteBank(String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankBalance(String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankHas(String playerName, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankWithdraw(String playerName, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankDeposit(String playerName, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public EconomyResponse isBankOwner(String playerName, String world) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public EconomyResponse isBankMember(String playerName, String world) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "DumbCoin does not support bank accounts!");
    }

    @Override
    public List<String> getBanks() {
        return null;
    }
}
