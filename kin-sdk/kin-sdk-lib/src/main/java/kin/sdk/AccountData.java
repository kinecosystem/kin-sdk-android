package kin.sdk;

import java.util.Arrays;
import java.util.Map;
import kin.base.responses.AccountResponse;
import kin.base.responses.AccountResponse.Flags;
import kin.base.responses.AccountResponse.Signer;

public class AccountData {

    private String publicAddress;
    private long sequenceNumber;
    private String pagingToken;
    private Integer subentryCount;
    private AccountResponse.Thresholds thresholds;
    private AccountResponse.Flags flags;
    private AccountResponse.Balance[] balances;
    private AccountResponse.Signer[] signers;
    private Map<String, String> data;

    public AccountData(String publicAddress, long sequenceNumber, String pagingToken, Integer subentryCount,
        AccountResponse.Thresholds thresholds, Flags flags, AccountResponse.Balance[] balances,
        Signer[] signers, Map<String, String> data) {
        this.publicAddress = publicAddress;
        this.sequenceNumber = sequenceNumber;
        this.pagingToken = pagingToken;
        this.subentryCount = subentryCount;
        this.thresholds = thresholds;
        this.flags = flags;
        this.balances = balances;
        this.signers = signers;
        this.data = data;
    }

    public String publicAddress() {
        return publicAddress;
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    public String pagingToken() {
        return pagingToken;
    }

    public Integer subentryCount() {
        return subentryCount;
    }

    public AccountResponse.Thresholds thresholds() {
        return thresholds;
    }

    public Flags flags() {
        return flags;
    }

    public AccountResponse.Balance[] balances() {
        return balances;
    }

    public Signer[] signers() {
        return signers;
    }

    public Map<String, String> data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountData that = (AccountData) o;
        return sequenceNumber == that.sequenceNumber &&
            publicAddress.equals(that.publicAddress) &&
            pagingToken.equals(that.pagingToken) &&
            subentryCount.equals(that.subentryCount) &&
            thresholds.equals(that.thresholds) &&
            flags.equals(that.flags) &&
            Arrays.equals(balances, that.balances) &&
            Arrays.equals(signers, that.signers) &&
            data.equals(that.data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((publicAddress == null) ? 0 : publicAddress.hashCode());
        result = prime * result + Long.valueOf(sequenceNumber).hashCode();
        result = prime * result + ((pagingToken == null) ? 0 : pagingToken.hashCode());
        result = prime * result + ((subentryCount == null) ? 0 : subentryCount.hashCode());
        result = prime * result + ((thresholds == null) ? 0 : thresholds.hashCode());
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + Arrays.hashCode(balances);
        result = prime * result + Arrays.hashCode(signers);
        return result;
    }
}
