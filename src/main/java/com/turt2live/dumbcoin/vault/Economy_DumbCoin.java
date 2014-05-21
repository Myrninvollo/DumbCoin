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

package com.turt2live.dumbcoin.vault;

import com.turt2live.dumbcoin.DumbCoin;
import com.turt2live.hurtle.uuid.UUIDUtils;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.List;

public class Economy_DumbCoin extends AbstractEconomy {

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
        return plugin.getBalanceManager().getBalance(UUIDUtils.getUUID(playerName));
    }

    @Override
    public boolean has(String playerName, double v) {
        return plugin.getBalanceManager().hasEnough(UUIDUtils.getUUID(playerName), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double v) {
        plugin.getBalanceManager().withdraw(UUIDUtils.getUUID(playerName), v);
        return new EconomyResponse(v, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double v) {
        plugin.getBalanceManager().deposit(UUIDUtils.getUUID(playerName), v);
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
