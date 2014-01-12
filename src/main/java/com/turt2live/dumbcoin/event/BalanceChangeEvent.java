package com.turt2live.dumbcoin.event;

import com.turt2live.commonsense.event.DumbNotCancellableEvent;
import com.turt2live.dumbcoin.DumbCoin;

/**
 * Fired when a balance is changed. This is done post-set.
 *
 * @author turt2live
 */
public class BalanceChangeEvent extends DumbNotCancellableEvent {

    private String account;
    private double difference;

    /**
     * Creates a new BalanceChangeEvent
     *
     * @param account    the account being modified
     * @param difference the difference applied to the balance
     */
    public BalanceChangeEvent(String account, double difference) {
        if (account == null || difference == 0) throw new IllegalArgumentException();
        this.account = account;
        this.difference = difference;
    }

    /**
     * Gets the account this change was applied to
     *
     * @return the account this change was applied to
     */
    public String getAccount() {
        return account;
    }

    /**
     * Gets the balance difference on the account
     *
     * @return the balance difference
     */
    public double getDifference() {
        return difference;
    }

    /**
     * Gets the balance the account had before the event was fired
     *
     * @return the balance pre-change
     */
    public double getBalanceBeforeChange() {
        return DumbCoin.p.getBalanceManager().getBalance(account) - difference;
    }

}
