package kin.sdk.models

import java.math.BigDecimal

data class Balance(val value: BigDecimal) {
    fun value(precision: Int): String {
        return value.setScale(precision, BigDecimal.ROUND_FLOOR).toString()
    }
}
