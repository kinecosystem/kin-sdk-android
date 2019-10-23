package kin.sdk.models;

import java.math.BigDecimal;

public final class Balance {

    private BigDecimal valueInKin;

    public Balance(BigDecimal valueInKin) {
        this.valueInKin = valueInKin;
    }

    public BigDecimal value() {
        return valueInKin;
    }

    public String value(int precision) {
        return valueInKin.setScale(precision, BigDecimal.ROUND_FLOOR).toString();
    }
}
