package com.turt2live.dumbcoin;

import java.util.List;

public abstract class FutureBalances {

    public abstract void accept(List<TopBalanceManager.PlayerBalance> balances, int minRank, int maxRank);

}
