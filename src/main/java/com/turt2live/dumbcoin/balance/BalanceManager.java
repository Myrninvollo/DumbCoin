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

    public abstract double getBalanceNoStart(UUID player);

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
