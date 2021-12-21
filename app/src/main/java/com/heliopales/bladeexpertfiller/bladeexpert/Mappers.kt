package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.damages.DamageTypeCategory
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType
import com.heliopales.bladeexpertfiller.secondaryentities.Severity

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
        interventionId = bladeWrapper.interventionId,
        position = bladeWrapper.position,
        serial = bladeWrapper.serial
    )
}

fun mapBladeExpertSeverity(severityWrapper: SeverityWrapper): Severity {
    return Severity(
        id = severityWrapper.id,
        alias = severityWrapper.alias,
        color = severityWrapper.color,
        fontColor = severityWrapper.fontColor
    )
}

fun mapBladeExpertDamageType(damageTypeWrapper: DamageTypeWrapper): DamageType {
    return DamageType(
        id = damageTypeWrapper.id,
        category = DamageTypeCategory.getDamageTypeCategoryByCode(damageTypeWrapper.categoryCode),
        name = damageTypeWrapper.name,
        inheritType = damageTypeWrapper.inheritType
    )
}