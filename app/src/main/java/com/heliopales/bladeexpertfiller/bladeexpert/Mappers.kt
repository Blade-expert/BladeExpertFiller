package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.damages.DamageSpotCondition
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

fun mapToBladeExpertIntervention(intervention: Intervention): InterventionWrapper {
    return InterventionWrapper(
        id = intervention.id,
        turbineName = intervention.turbineName,
        turbineSerial = intervention.turbineSerial,
        blades = null
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

fun mapToBladeExpertBlade(blade: Blade): BladeWrapper {
    return BladeWrapper(
        id = blade.id,
        interventionId = blade.interventionId,
        serial = blade.serial,
        position = blade.position
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

fun mapToBladeExpertDamageSpotCondition(dsc: DamageSpotCondition): DamageSpotConditionWrapper{
    return DamageSpotConditionWrapper(
        id = dsc.id,
        fieldCode = dsc.fieldCode,
        interventionId = dsc.interventionId,
        bladeId = dsc.bladeId,
        severityId = dsc.severityId,
        description = dsc.description,
        damageTypeId = dsc.damageTypeId,
        radialPosition = dsc.radialPosition,
        radialLength = dsc.radialLength,
        longitudinalLength = dsc.radialLength,
        repetition = dsc.repetition,
        position = dsc.position,
        profileDepth = dsc.profileDepth
    )
}