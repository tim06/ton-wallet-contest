package org.ton.java.smartcontract.wallet;

import org.ton.java.smartcontract.types.WalletVersion;

public class Wallet {

    Options options;
    WalletVersion walletVersion;

    public Wallet(WalletVersion walletVersion, Options options) {
        this.walletVersion = walletVersion;
        this.options = options;
    }

    public Wallet(WalletVersion walletVersion) {
        this.walletVersion = walletVersion;
        this.options = new Options();
    }

    public WalletV4ContractR2 create() {

        return new WalletV4ContractR2(options);
    }
}
