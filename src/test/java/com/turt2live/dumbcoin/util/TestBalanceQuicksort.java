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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class TestBalanceQuicksort {

    private List<TopBalanceManager.PlayerBalance> balances;
    private int elements = 10;

    @Before
    public void setUp() throws Exception {
        balances = new ArrayList<TopBalanceManager.PlayerBalance>();
        Random generator = new Random();
        for (int i = 0; i < elements; i++) {
            balances.add(new TopBalanceManager.PlayerBalance("Player" + i, generator.nextDouble() * generator.nextInt(100000), i));
        }
    }

    @Test
    public void testNull() {
        BalanceQuicksort sorter = new BalanceQuicksort();
        sorter.sort(null);
    }

    @Test
    public void testEmpty() {
        BalanceQuicksort sorter = new BalanceQuicksort();
        sorter.sort(new ArrayList<TopBalanceManager.PlayerBalance>());
    }

    @Test
    public void testSimpleElement() {
        BalanceQuicksort sorter = new BalanceQuicksort();
        List<TopBalanceManager.PlayerBalance> test = new ArrayList<TopBalanceManager.PlayerBalance>();
        test.add(new TopBalanceManager.PlayerBalance("PlayerTest", 1000, 1));
        sorter.sort(test);
    }

    @Test
    public void testQuickSort() {
        long startTime = System.currentTimeMillis();

        BalanceQuicksort sorter = new BalanceQuicksort();
        sorter.sort(balances);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Quicksort " + elapsedTime);

        if (!validate(balances)) {
            fail("Should not happen");
        }
        assertTrue(true);
    }

    private boolean validate(List<TopBalanceManager.PlayerBalance> balances) {
        TopBalanceManager.PlayerBalance last = balances.get(0);
        for (int i = 1; i < balances.size(); i++) {
            TopBalanceManager.PlayerBalance current = balances.get(i);
            System.out.println(current + " |||| " + last);
            if (current.getBalance() > last.getBalance() || current.getRank() < last.getRank()) {
                return false;
            }
            last = current;
        }
        return true;
    }

    private void printResult(int[] numbers) {
        for (int i = 0; i < numbers.length; i++) {
            System.out.print(numbers[i]);
        }
        System.out.println();
    }
}
