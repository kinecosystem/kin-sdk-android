package kin.base.responses;

import com.google.gson.annotations.SerializedName;
import kin.base.Server;

/**
 * Represents controlled account response.
 *
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/account.html" target="_blank">Account
 * documentation</a>
 * @see Server#accounts()
 */
public class ControlledAccountsResponse {


    @SerializedName("_embedded")
    private Records records;
    @SerializedName("_links")
    private Links links;

    public ControlledAccount[] getControlledAccounts() {
        return records.getControlledAccounts();
    }

    /**
     * Represents records of aggregated balances.
     */
    public static class Records {

        @SerializedName("records")
        private ControlledAccount[] controlledAccounts;

        public ControlledAccount[] getControlledAccounts() {
            return controlledAccounts;
        }
    }

    /**
     * Represents account aggregated balance.
     */
    public static class ControlledAccount {

        @SerializedName("account_id")
        private String accountId;
        @SerializedName("balance")
        private String balance;

        public String getAccountId() {
            return accountId;
        }

        public String getBalance() {
            return balance;
        }
    }

    public Links getLinks() {
        return links;
    }

    /**
     * Links connected to account.
     */
    public static class Links {

        @SerializedName("self")
        private final Link self;
        @SerializedName("next")
        private final Link next;
        @SerializedName("prev")
        private final Link prev;

        Links(Link self, Link next, Link prev) {
            this.self = self;
            this.next = next;
            this.prev = prev;
        }

        public Link getSelf() {
            return self;
        }

        public Link getNext() {
            return next;
        }

        public Link getPrev() {
            return prev;
        }
    }
}

