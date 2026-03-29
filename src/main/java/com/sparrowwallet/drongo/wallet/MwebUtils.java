package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.KeyPurpose;
import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.protocol.Sha256Hash;
import com.sparrowwallet.drongo.protocol.TransactionOutput;
import com.sparrowwallet.drongo.psbt.PSBT;

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

    public static void psbtCopy(PSBT psbt, PSBT psbt2) {
        var inputs = psbt.getPsbtInputs();
        var inputs2 = psbt2.getPsbtInputs();
        var outputs = psbt.getPsbtOutputs();
        var outputs2 = psbt2.getPsbtOutputs();

        if (inputs.size() != inputs2.size()) return;
        if (outputs.size() != outputs2.size()) return;

        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).isMweb() != inputs2.get(i).isMweb()) return;
        }
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).isMweb() != outputs2.get(i).isMweb()) return;
        }
        for (int i = 0; i < inputs.size(); i++) {
            inputs2.get(i).setMwebAmount(inputs.get(i).getMwebAmount());
        }
        for (int i = 0; i < outputs.size(); i++) {
            outputs2.get(i).setMwebStealthAddress(outputs.get(i).getMwebStealthAddress());
        }
    }
}
