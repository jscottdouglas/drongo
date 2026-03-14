package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.protocol.Bech32;
import com.sparrowwallet.drongo.protocol.ScriptType;

public class MwebAddress extends Address {
    public MwebAddress(byte[] pubKeys) {
        super(pubKeys);
    }

    @Override
    public int getVersion(Network network) {
        return 0;
    }

    @Override
    public String getAddress(Network network) {
        return Bech32.encode(network.getMwebAddressHrp(), getVersion(), data);
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.MWEB;
    }

    @Override
    public String getOutputScriptDataType() {
        return "MWEB";
    }
}
