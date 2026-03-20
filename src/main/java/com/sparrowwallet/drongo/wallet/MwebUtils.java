package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.KeyPurpose;
import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.protocol.Sha256Hash;
import com.sparrowwallet.drongo.protocol.TransactionOutput;

public class MwebUtils {
    public static Sha256Hash getOutputId(Wallet wallet, BlockTransactionHashIndex utxo) {
        return wallet.getWalletTransaction(utxo.getHash()).getTransaction()
                .getOutputs().get((int)utxo.getIndex()).getMwebOutputId();
    }

    public static int getAddressIndex(WalletNode node) {
        if (node.getKeyPurpose() == KeyPurpose.CHANGE) return 0;
        return node.getDerivation().getLast().i() + 1;
    }

    public static TransactionOutput adjustScript(TransactionOutput out) {
        if (!ScriptType.MWEB.isScriptType(out.getScript())) return out;
        return new TransactionOutput(null, out.getValue(),
                ScriptType.MWEB.getHashFromScript(out.getScript()));
    }
}
