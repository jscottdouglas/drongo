package com.sparrowwallet.drongo.psbt;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.crypto.ECKey;
import com.sparrowwallet.drongo.crypto.SchnorrSignature;
import com.sparrowwallet.drongo.protocol.*;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.Blake3Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sparrowwallet.drongo.psbt.PSBTEntry.populateEntry;

public class PSBTKernel {
    public static final byte PSBT_KERNEL_MWEB_EXCESS_COMMIT = 0x00;
    public static final byte PSBT_KERNEL_MWEB_STEALTH_EXCESS = 0x01;
    public static final byte PSBT_KERNEL_MWEB_FEE = 0x02;
    public static final byte PSBT_KERNEL_MWEB_PEGIN_AMOUNT = 0x03;
    public static final byte PSBT_KERNEL_MWEB_PEGOUT = 0x04;
    public static final byte PSBT_KERNEL_MWEB_LOCK_HEIGHT = 0x05;
    public static final byte PSBT_KERNEL_MWEB_FEATURES = 0x06;
    public static final byte PSBT_KERNEL_MWEB_EXTRA_DATA = 0x07;
    public static final byte PSBT_KERNEL_MWEB_SIGNATURE = 0x08;

    private Byte mwebFeatures;
    private byte[] mwebExcessCommit;
    private ECKey mwebStealthExcess;
    private Long mwebFee;
    private Long mwebPeginAmount;
    private List<TransactionOutput> mwebPegOuts = new ArrayList<>();
    private Long mwebLockHeight;
    private byte[] mwebExtraData;
    private SchnorrSignature mwebSignature;

    private final PSBT psbt;
    private int index;

    private static final Logger log = LoggerFactory.getLogger(PSBTKernel.class);

    PSBTKernel(PSBT psbt, int index) {
        this.psbt = psbt;
        this.index = index;
    }

    PSBTKernel(PSBT psbt, List<PSBTEntry> inputEntries, int index) throws PSBTParseException {
        this(psbt, index);

        for(PSBTEntry entry : inputEntries) {
            switch((byte)entry.getKeyType()) {
                case PSBT_KERNEL_MWEB_EXCESS_COMMIT:
                    entry.checkOneByteKey();
                    this.mwebExcessCommit = entry.getData();
                    log.debug("Found kernel mweb excess commit " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_STEALTH_EXCESS:
                    entry.checkOneByteKey();
                    this.mwebStealthExcess = ECKey.fromPublicOnly(entry.getData());
                    log.debug("Found kernel mweb stealth excess " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_FEE:
                    entry.checkOneByteKey();
                    if(entry.getData().length != 8) {
                        throw new PSBTParseException("PSBT kernel mweb fee must be 8 bytes");
                    }
                    this.mwebFee = Utils.readInt64(entry.getData(), 0);
                    log.debug("Found kernel mweb fee " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_PEGIN_AMOUNT:
                    entry.checkOneByteKey();
                    if(entry.getData().length != 8) {
                        throw new PSBTParseException("PSBT kernel mweb pegin amount must be 8 bytes");
                    }
                    this.mwebPeginAmount = Utils.readInt64(entry.getData(), 0);
                    log.debug("Found kernel mweb pegin amount " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_PEGOUT:
                    this.mwebPegOuts.add(new TransactionOutput(null, entry.getData(), 0));
                    log.debug("Found kernel mweb pegout " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_LOCK_HEIGHT:
                    entry.checkOneByteKey();
                    if(entry.getData().length != 4) {
                        throw new PSBTParseException("PSBT kernel mweb lock height must be 4 bytes");
                    }
                    this.mwebLockHeight = Utils.readUint32(entry.getData(), 0);
                    log.debug("Found kernel mweb lock height " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_FEATURES:
                    entry.checkOneByteKey();
                    if(entry.getData().length != 1) {
                        throw new PSBTParseException("PSBT kernel mweb kernel features must be 1 byte");
                    }
                    this.mwebFeatures = entry.getData()[0];
                    log.debug("Found kernel mweb kernel features " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_EXTRA_DATA:
                    entry.checkOneByteKey();
                    this.mwebExtraData = entry.getData();
                    log.debug("Found kernel mweb kernel extra data " + Utils.bytesToHex(entry.getData()));
                    break;
                case PSBT_KERNEL_MWEB_SIGNATURE:
                    entry.checkOneByteKey();
                    this.mwebSignature = SchnorrSignature.decode(entry.getData());
                    log.debug("Found kernel mweb kernel signature " + Utils.bytesToHex(entry.getData()));
                    break;
                default:
                    log.warn("PSBT kernel not recognized key type: " + entry.getKeyType());
            }
        }
    }

    public List<PSBTEntry> getKernelEntries(int psbtVersion) {
        List<PSBTEntry> entries = new ArrayList<>();

        if(psbtVersion >= 2) {
            if(mwebExcessCommit != null) {
                entries.add(populateEntry(PSBT_KERNEL_MWEB_EXCESS_COMMIT, null, mwebExcessCommit));
            }
            if(mwebStealthExcess != null) {
                entries.add(populateEntry(PSBT_KERNEL_MWEB_STEALTH_EXCESS, null, mwebStealthExcess.getPubKey(true)));
            }
            if(mwebFee != null) {
                byte[] bs = new byte[8];
                Utils.int64ToByteArrayLE(mwebFee, bs, 0);
                entries.add(populateEntry(PSBT_KERNEL_MWEB_FEE, null, bs));
            }
            if(mwebPeginAmount != null) {
                byte[] bs = new byte[8];
                Utils.int64ToByteArrayLE(mwebPeginAmount, bs, 0);
                entries.add(populateEntry(PSBT_KERNEL_MWEB_PEGIN_AMOUNT, null, bs));
            }
            for (var pegOut : mwebPegOuts) {
                entries.add(populateEntry(PSBT_KERNEL_MWEB_PEGOUT, null, pegOut.bitcoinSerialize()));
            }
            if(mwebLockHeight != null) {
                byte[] bs = new byte[4];
                Utils.uint32ToByteArrayLE(mwebLockHeight, bs, 0);
                entries.add(populateEntry(PSBT_KERNEL_MWEB_LOCK_HEIGHT, null, bs));
            }
            if(mwebFeatures != null) {
                entries.add(populateEntry(PSBT_KERNEL_MWEB_FEATURES, null, new byte[]{mwebFeatures}));
            }
            if(mwebExtraData != null) {
                entries.add(populateEntry(PSBT_KERNEL_MWEB_EXTRA_DATA, null, mwebExtraData));
            }
            if(mwebSignature != null) {
                entries.add(populateEntry(PSBT_KERNEL_MWEB_SIGNATURE, null, mwebSignature.encode()));
            }
        }

        return entries;
    }

    void combine(PSBTKernel psbtKernel) {
        if(psbtKernel.mwebFeatures != null) {
            mwebFeatures = psbtKernel.mwebFeatures;
        }

        if(psbtKernel.mwebExcessCommit != null) {
            mwebExcessCommit = psbtKernel.mwebExcessCommit;
        }

        if(psbtKernel.mwebStealthExcess != null) {
            mwebStealthExcess = psbtKernel.mwebStealthExcess;
        }

        if(psbtKernel.mwebFee != null) {
            mwebFee = psbtKernel.mwebFee;
        }

        if(psbtKernel.mwebPeginAmount != null) {
            mwebPeginAmount = psbtKernel.mwebPeginAmount;
        }

        if(psbtKernel.mwebPegOuts != null) {
            mwebPegOuts = psbtKernel.mwebPegOuts;
        }

        if(psbtKernel.mwebLockHeight != null) {
            mwebLockHeight = psbtKernel.mwebLockHeight;
        }

        if(psbtKernel.mwebExtraData != null) {
            mwebExtraData = psbtKernel.mwebExtraData;
        }

        if(psbtKernel.mwebSignature != null) {
            mwebSignature = psbtKernel.mwebSignature;
        }
    }

    public List<TransactionOutput> getPegOuts() {
        return Collections.unmodifiableList(mwebPegOuts);
    }

    public boolean isFinalized() {
        return mwebSignature != null;
    }

    private void writeVarInt(Digest hash, long n) {
        byte[] buf = new byte[10];
        int i = 0;
        for(;; i++) {
            buf[i] = (byte)(n & 0x7f);
            if(i > 0) buf[i] |= (byte)0x80;
            if(n < 0x80) break;
            n = (n >> 7) - 1;
        }
        for(; i >= 0; i--) {
            hash.update(buf[i]);
        }
    }

    private void updateHash(Digest hash, byte[] arr) {
        hash.update(arr, 0, arr.length);
    }

    public Sha256Hash getHash() {
        Digest hash = new Blake3Digest();
        hash.update(mwebFeatures);
        if(mwebFee != null) {
            writeVarInt(hash, mwebFee);
        }
        if(mwebPeginAmount != null) {
            writeVarInt(hash, mwebPeginAmount);
        }
        if(!mwebPegOuts.isEmpty()) {
            updateHash(hash, new VarInt(mwebPegOuts.size()).encode());
            mwebPegOuts.forEach(pegOut -> {
                writeVarInt(hash, pegOut.getValue());
                updateHash(hash, new VarInt(pegOut.getScriptBytes().length).encode());
                updateHash(hash, pegOut.getScriptBytes());
            });
        }
        if(mwebLockHeight != null) {
            writeVarInt(hash, mwebLockHeight);
        }
        if(mwebStealthExcess != null) {
            updateHash(hash, mwebStealthExcess.getPubKey(true));
        }
        if(mwebExtraData != null) {
            updateHash(hash, mwebExtraData);
        }
        updateHash(hash, mwebExcessCommit);
        updateHash(hash, mwebSignature.encode());
        byte[] result = new byte[32];
        hash.doFinal(result, 0);
        return Sha256Hash.wrap(result);
    }
}
