package com.sparrowwallet.drongo.crypto;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.Blake3Digest;

public class MwebAddressDeriver {
    public static byte[] Derive(ECKey scan, ECKey spendPub, int index) {
        Digest hash = new Blake3Digest();
        hash.update((byte)'A');
        hash.update((byte)(index));
        hash.update((byte)(index >> 8));
        hash.update((byte)(index >> 16));
        hash.update((byte)(index >> 24));
        hash.update(scan.getPrivKeyBytes(), 0, 32);
        byte[] m = new byte[32];
        hash.doFinal(m, 0);
        ECKey b = spendPub.add(ECKey.fromPrivate(m));
        ECKey a = b.multiply(scan.getPrivKey());
        byte[] result = new byte[66];
        System.arraycopy(a.getPubKey(true), 0, result, 0, 33);
        System.arraycopy(b.getPubKey(true), 0, result, 33, 33);
        return result;
    }
}
