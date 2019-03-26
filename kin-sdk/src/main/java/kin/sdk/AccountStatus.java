package kin.sdk;

public enum AccountStatus
{
    /**
     * Account was not created on blockchain network, account should be created and funded by a different account on
     * the blockchain.
     */
    NOT_CREATED(0),
    /**
     * Account was created, account is ready to use with kin.
     */
    CREATED(2);

    private final int value;

    AccountStatus(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        String value = "";
        switch (this) {
            case CREATED:
                value = "Created";
                break;
            case NOT_CREATED:
                value = "Not Created";
                break;
        }
        return value;
    }
}

