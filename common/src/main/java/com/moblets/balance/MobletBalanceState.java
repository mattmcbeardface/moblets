package com.moblets.balance;

public interface MobletBalanceState {
    boolean moblets$isBalanceActive();

    void moblets$activateBalance();

    long moblets$getBalanceRevision();

    boolean moblets$getBalanceTamed();

    void moblets$markBalanceApplied(
            long revision,
            boolean tamed
    );
}
