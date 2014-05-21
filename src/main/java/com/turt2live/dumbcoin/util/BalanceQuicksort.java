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

package com.turt2live.dumbcoin.util;

import com.turt2live.dumbcoin.TopBalanceManager;

import java.util.List;

public class BalanceQuicksort {

    private List<TopBalanceManager.PlayerBalance> balances;
    private int number;

    public void sort(List<TopBalanceManager.PlayerBalance> balances) {
        if (balances == null || balances.size() == 0) {
            return;
        }
        this.balances = balances;
        number = balances.size();
        quicksort(0, number - 1);
    }

    private void quicksort(int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        TopBalanceManager.PlayerBalance pivot = balances.get(low + (high - low) / 2);

        // Divide into two lists
        while (i <= j) {
            TopBalanceManager.PlayerBalance pi = balances.get(i).clone();
            TopBalanceManager.PlayerBalance pj = balances.get(j).clone();
            while (pi.getBalance() > pivot.getBalance()) {
                i++;
                pi = balances.get(i).clone();
            }
            while (pj.getBalance() < pivot.getBalance()) {
                j--;
                pj = balances.get(j).clone();
            }
            if (i <= j) {
                balances.set(i, pj.clone(i));
                balances.set(j, pi.clone(j));
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort(low, j);
        if (i < high)
            quicksort(i, high);
    }
}
