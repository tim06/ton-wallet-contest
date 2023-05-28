package org.ton.java.smartcontract.wallet;

import org.ton.java.address.Address;
import org.ton.java.cell.Cell;
import org.ton.java.cell.CellBuilder;
import org.ton.java.smartcontract.types.DeployedPlugin;
import org.ton.java.smartcontract.types.ExternalMessage;
import org.ton.java.smartcontract.types.NewPlugin;
import org.ton.java.smartcontract.types.WalletCodes;
import org.ton.java.smartcontract.wallet.Options;
import org.ton.java.smartcontract.wallet.WalletContract;
import org.ton.java.tonlib.Tonlib;
import org.ton.java.tonlib.types.*;

import java.math.BigInteger;
import java.util.*;

import static java.util.Objects.isNull;

public class WalletV4ContractR2 implements WalletContract {

    Options options;
    Address address;

    /**
     * @param options Options
     */
    public WalletV4ContractR2(Options options) {
        this.options = options;
        options.code = Cell.fromBoc(WalletCodes.V4R2.getValue());
        if (isNull(options.walletId)) {
            options.walletId = 698983191 + options.wc;
        }
    }

    @Override
    public String getName() {
        return "V4R2";
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public Address getAddress() {
        if (isNull(address)) {
            return (createStateInit()).address;
        }
        return address;
    }

    @Override
    public Cell createDataCell() {
        CellBuilder cell = CellBuilder.beginCell();
        cell.storeUint(BigInteger.ZERO, 32);
        cell.storeUint(BigInteger.valueOf(getOptions().walletId), 32);
        cell.storeBytes(getOptions().publicKey);
        cell.storeUint(BigInteger.ZERO, 1); //plugins dict empty
        return cell.endCell();
    }

    @Override
    public Cell createSigningMessage(long seqno) {
        return createSigningMessage(seqno, false);
    }

    /**
     * @param seqno     long
     * @param withoutOp boolean
     * @return Cell
     */

    public Cell createSigningMessage(long seqno, boolean withoutOp) {

        CellBuilder message = CellBuilder.beginCell();

        message.storeUint(BigInteger.valueOf(getOptions().walletId), 32);

        if (seqno == 0) {
            for (int i = 0; i < 32; i++) {
                message.storeBit(true);
            }
        } else {
            Date date = new Date();
            long timestamp = (long) Math.floor(date.getTime() / 1e3);
            message.storeUint(BigInteger.valueOf(timestamp + 60L), 32);
        }

        message.storeUint(BigInteger.valueOf(seqno), 32);

        if (!withoutOp) {
            message.storeUint(BigInteger.ZERO, 8); // op
        }

        return message.endCell();
    }

    /**
     * Deploy and install/assigns subscription plugin.
     * One can also deploy plugin separately and later install into the wallet. See installPlugin().
     *
     * @param params NewPlugin
     */
    public void deployAndInstallPlugin(Tonlib tonlib, NewPlugin params) {

        Cell signingMessage = createSigningMessage(params.seqno, true);
        signingMessage.bits.writeUint(BigInteger.ONE, 8); // op
        signingMessage.bits.writeInt(BigInteger.valueOf(params.pluginWc), 8);
        signingMessage.bits.writeCoins(params.amount); // plugin balance
        signingMessage.refs.add(params.stateInit);
        signingMessage.refs.add(params.body);
        ExternalMessage extMsg = createExternalMessage(signingMessage, params.secretKey, params.seqno, false);

        tonlib.sendRawMessage(extMsg.message.toBocBase64(false));
    }

    public Cell createPluginBody() {
        CellBuilder body = CellBuilder.beginCell(); // mgsBody in simple-subscription-plugin.fc is not used
        body.storeUint(new BigInteger("706c7567", 16).add(new BigInteger("80000000", 16)), 32); //OP
        return body.endCell();
    }

    public Cell createPluginSelfDestructBody() {
        return CellBuilder.beginCell().storeUint(0x64737472, 32).endCell();
    }

    /**
     * @param params    DeployedPlugin,
     * @param isInstall boolean install or uninstall
     */
    ExternalMessage setPlugin(DeployedPlugin params, boolean isInstall) {

        Cell signingMessage = createSigningMessage(params.seqno, true);
        signingMessage.bits.writeUint(isInstall ? BigInteger.valueOf(2) : BigInteger.valueOf(3), 8); // op
        signingMessage.bits.writeInt(BigInteger.valueOf(params.pluginAddress.wc), 8);
        signingMessage.bits.writeBytes(params.pluginAddress.hashPart);
        signingMessage.bits.writeCoins(BigInteger.valueOf(params.amount.longValue()));
        signingMessage.bits.writeUint(BigInteger.valueOf(params.queryId), 64);

        return this.createExternalMessage(signingMessage, params.secretKey, params.seqno, false);
    }

    /**
     * Installs/assigns plugin into wallet-v4
     *
     * @param params DeployedPlugin
     */
    public ExternalMessage installPlugin(DeployedPlugin params) {
        return setPlugin(params, true);
    }

    /**
     * Uninstalls/removes plugin from wallet-v4
     *
     * @param params DeployedPlugin
     */
    public ExternalMessage removePlugin(DeployedPlugin params) {
        return setPlugin(params, false);
    }


    /**
     * @return subwallet-id long
     */
    public long getWalletId(Tonlib tonlib) {

        Address myAddress = getAddress();
        RunResult result = tonlib.runMethod(myAddress, "get_subwallet_id");
        TvmStackEntryNumber subWalletId = (TvmStackEntryNumber) result.getStack().get(0);

        return subWalletId.getNumber().longValue();
    }

    public byte[] getPublicKey(Tonlib tonlib) {
        Address myAddress = getAddress();
        RunResult result = tonlib.runMethod(myAddress, "get_public_key");
        TvmStackEntryNumber pubKey = (TvmStackEntryNumber) result.getStack().get(0);

        return pubKey.getNumber().toByteArray();
    }

    /**
     * @param pluginAddress Address
     * @return boolean
     */
    public boolean isPluginInstalled(Tonlib tonlib, Address pluginAddress) {
        String hashPart = new BigInteger(pluginAddress.hashPart).toString();

        Address myAddress = getAddress();

        Deque<String> stack = new ArrayDeque<>();
        stack.offer("[num, " + pluginAddress.wc + "]");
        stack.offer("[num, " + hashPart + "]");

        RunResult result = tonlib.runMethod(myAddress, "is_plugin_installed", stack);
        TvmStackEntryNumber resultNumber = (TvmStackEntryNumber) result.getStack().get(0);

        return resultNumber.getNumber().longValue() != 0;
    }

    /**
     * @return List<String> plugins addresses
     */
    public List<String> getPluginsList(Tonlib tonlib) {
        List<String> r = new ArrayList<>();
        Address myAddress = getAddress();
        RunResult result = tonlib.runMethod(myAddress, "get_plugin_list");
        TvmStackEntryList list = (TvmStackEntryList) result.getStack().get(0);
        for (Object o : list.getList().getElements()) {
            TvmStackEntryTuple t = (TvmStackEntryTuple) o;
            TvmTuple tuple = t.getTuple();
            TvmStackEntryNumber wc = (TvmStackEntryNumber) tuple.getElements().get(0); // 1 byte
            TvmStackEntryNumber addr = (TvmStackEntryNumber) tuple.getElements().get(1); // 32 bytes
            r.add(wc.getNumber() + ":" + addr.getNumber().toString(16).toUpperCase());
        }
        return r;
    }

    public Cell createPluginDataCell(Address wallet,
                                     Address beneficiary,
                                     BigInteger amount,
                                     long period,
                                     long startTime,
                                     long timeOut,
                                     long lastPaymentTime,
                                     long lastRequestTime,
                                     long failedAttempts,
                                     long subscriptionId) {

        CellBuilder cell = CellBuilder.beginCell();
        cell.storeAddress(wallet);
        cell.storeAddress(beneficiary);
        cell.storeCoins(amount);
        cell.storeUint(BigInteger.valueOf(period), 32);
        cell.storeUint(BigInteger.valueOf(startTime), 32);
        cell.storeUint(BigInteger.valueOf(timeOut), 32);
        cell.storeUint(BigInteger.valueOf(lastPaymentTime), 32);
        cell.storeUint(BigInteger.valueOf(lastRequestTime), 32);
        cell.storeUint(BigInteger.valueOf(failedAttempts), 8);
        cell.storeUint(BigInteger.valueOf(subscriptionId), 32);
        return cell.endCell();
    }

    /**
     * Sends amount of nano toncoins to destination address using auto-fetched seqno without the body and default send-mode 3
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount) {
        long seqno = getSeqno(tonlib);
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno);
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }

    /**
     * Sends amount of nano toncoins to destination address using specified seqno with the body and default send-mode 3
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     * @param body               byte[]
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount, long seqno, byte[] body) {
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno, body);
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }

    /**
     * Sends amount of nano toncoins to destination address using specified seqno with the body and specified send-mode
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     * @param body               byte[]
     * @param sendMode           byte
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount, long seqno, byte[] body, byte sendMode) {
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno, body, sendMode);
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }

    /**
     * Sends amount of nano toncoins to destination address using auto-fetched seqno with the body and default send-mode 3
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     * @param body               byte[]
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount, byte[] body) {
        long seqno = getSeqno(tonlib);
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno, body);
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }

    /**
     * Sends amount of nano toncoins to destination address using specified seqno with the comment and default send-mode 3
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     * @param seqno              long
     * @param comment            String
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount, long seqno, String comment) {
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno, CellBuilder.beginCell().storeUint(0, 32).storeString(comment).endCell());
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }

    /**
     * Sends amount of nano toncoins to destination address using specified seqno without the comment and default send-mode 3
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     * @param seqno              long
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount, long seqno) {
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno);
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }

    /**
     * Sends amount of nano toncoins to destination address using auto-fetched seqno without the body and default send-mode 3
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     * @param comment            String
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount, String comment) {
        long seqno = getSeqno(tonlib);
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno, CellBuilder.beginCell().storeUint(0, 32).storeString(comment).endCell());
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }

    /**
     * Sends amount of nano toncoins to destination address using auto-fetched seqno with the body and specified send-mode
     *
     * @param tonlib             Tonlib
     * @param secretKey          byte[]
     * @param destinationAddress Address
     * @param amount             BigInteger
     * @param body               byte[]
     * @param sendMode           byte
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, byte[] secretKey, Address destinationAddress, BigInteger amount, byte[] body, byte sendMode) {
        long seqno = getSeqno(tonlib);
        ExternalMessage msg = createTransferMessage(secretKey, destinationAddress, amount, seqno, body, sendMode);
        return tonlib.sendRawMessage(msg.message.toBocBase64(false));
    }
}
