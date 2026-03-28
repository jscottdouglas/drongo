package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.protocol.Transaction;

public class MwebPegoutTxoFilter implements TxoFilter {
    private final Wallet wallet;

    public MwebPegoutTxoFilter(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public boolean isEligible(BlockTransactionHashIndex candidate) {
        BlockTransaction blockTransaction = wallet.getWalletTransaction(candidate.getHash());
        if(blockTransaction != null && blockTransaction.getTransaction() != null && blockTransaction.getTransaction().isHogEx()
                && wallet.getStoredBlockHeight() != null && candidate.getConfirmations(wallet.getStoredBlockHeight()) < Transaction.PEGOUT_MATURITY_THRESHOLD) {
            return false;
        }

        return true;
    }
}
