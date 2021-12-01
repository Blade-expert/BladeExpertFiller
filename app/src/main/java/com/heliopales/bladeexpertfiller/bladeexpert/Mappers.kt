package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.intervention.Intervention

fun mapBladeExpertIntervention(interventionWrapper: InterventionWrapper): Intervention {
    return Intervention(
        id = interventionWrapper.id,
        turbineName = interventionWrapper.turbineName,
        turbineSerial = interventionWrapper.turbineSerial
    )
}


fun mapBladeExpertBlade(bladeWrapper: BladeWrapper): Blade {
    return Blade(
        id = bladeWrapper.id,
        position = bladeWrapper.position,
        serial = bladeWrapper.serial
    )
}