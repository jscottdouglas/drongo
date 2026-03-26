package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.protocol.Bech32;
import com.sparrowwallet.drongo.protocol.ScriptType;

public class MwebPeginAddress extends Address {
    public MwebPeginAddress(byte[] hash) {
        super(hash);
    }

    @Override
    public int getVersion(Network network) {
        return 9;
    }

    @Override
    public String getAddress(Network network) {
        return Bech32.encode(network.getBech32AddressHRP(), getVersion(), Bech32.Encoding.BECH32, data);
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.MWEB_PEGIN;
    }

    @Override
    public String getOutputScriptDataType() {
        return "MWEB PegIn";
    }
}
