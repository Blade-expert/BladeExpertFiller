package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.intervention.Intervention

fun mapBladeExpertIntervention(interventionWrapper: InterventionWrapper): Intervention {
    return Intervention(
        id = interventionWrapper.id,
        turbineName = interventionWrapper.turbineName
    )
}