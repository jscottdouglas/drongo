package com.sparrowwallet.drongo.wallet;

import java.util.List;
import java.util.Map;

public interface FeeEstimator {
    long calcFeeIncrease(Wallet wallet, Map<BlockTransactionHashIndex, WalletNode> selectedUtxos,
                         List<WalletTransaction.Output> outputs, double vSize, double feeRate);
}
