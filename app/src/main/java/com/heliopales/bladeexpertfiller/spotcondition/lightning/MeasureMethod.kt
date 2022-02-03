package com.heliopales.bladeexpertfiller.spotcondition.lightning

enum class MeasureMethod(val alias: String) {

    RCPT_TO_NAC_200MA("Receptor to nacelle - 200mA"),
    RCPT_TO_TWR_200MA("Receptor to tower base - 200mA"),
    RCPT_TO_ROOT_200MA("Receptor to blade root - 200mA"),
    RCPT_TO_NAC_10A("Receptor to nacelle - 10A"),
    RCPT_TO_TWR_10A("Receptor to tower base - 10A"),
    RCPT_TO_ROOT_10A("Receptor to blade root - 10A");

    override fun toString(): String {
        return alias
    }

}