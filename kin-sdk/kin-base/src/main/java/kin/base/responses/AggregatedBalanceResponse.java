package kin.base.responses;

import com.google.gson.annotations.SerializedName;
import kin.base.Server;

/**
 * Represents account aggregated balance response.
 *
 * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/account.html" target="_blank">Account
 * documentation</a>
 * @see Server#accounts()
 */
public class AggregatedBalanceResponse extends Response {

    @SerializedName("_embedded")
    private Records records;
    @SerializedName("_links")
    private Links links;

    public AggregatedBalance getAggregatedBalance() {
        return records.getAggregateBalances()[0];
    }

    /**
     * Represents records of aggregated balances.
     */
    public static class Records {

        @SerializedName("records")
        private AggregatedBalance[] aggregatedBalances;

        public AggregatedBalance[] getAggregateBalances() {
            return aggregatedBalances;
        }
    }

    /**
     * Represents account aggregated balance.
     */
    public static class AggregatedBalance {

        @SerializedName("account_id")
        private String accountId;
        @SerializedName("aggregate_balance")
        private String aggregateBalance;

        public String getAccountId() {
            return accountId;
        }

        public String getAggregateBalance() {
            return aggregateBalance;
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

