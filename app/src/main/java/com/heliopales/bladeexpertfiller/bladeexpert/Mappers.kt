package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageTypeCategory
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType
import com.heliopales.bladeexpertfiller.secondaryentities.Severity
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition

fun mapBladeExpertIntervention(interventionWrapper: InterventionWrapper): Intervention {
    return Intervention(
        id = interventionWrapper.id,
        turbineName = interventionWrapper.turbineName,
        turbineSerial = interventionWrapper.turbineSerial,
        changeTurbineSerialAllowed = interventionWrapper.changeTurbineSerialAllowed,
        changeTurbinePictureAllowed = interventionWrapper.changeTurbinePictureAllowed
    )
}

fun mapToBladeExpertIntervention(intervention: Intervention): InterventionWrapper {
    return InterventionWrapper(
        id = intervention.id,
        turbineName = intervention.turbineName,
        turbineSerial = intervention.turbineSerial,
        blades = null,
        changeTurbineSerialAllowed = intervention.changeTurbineSerialAllowed,
        changeTurbinePictureAllowed = intervention.changeTurbinePictureAllowed
    )
}

fun mapBladeExpertBlade(bladeWrapper: BladeWrapper): Blade {
    return Blade(
        id = bladeWrapper.id,
        interventionId = bladeWrapper.interventionId,
        position = bladeWrapper.position,
        serial = bladeWrapper.serial,
        changeSerialAllowed = bladeWrapper.changeSerialAllowed,
        changePictureAllowed = bladeWrapper.changePictureAllowed
    )
}

fun mapToBladeExpertBlade(blade: Blade): BladeWrapper {
    return BladeWrapper(
        id = blade.id,
        interventionId = blade.interventionId,
        serial = blade.serial,
        position = blade.position,
        changeSerialAllowed = blade.changeSerialAllowed,
        changePictureAllowed = blade.changePictureAllowed
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

fun mapToBladeExpertDrainholeSpotCondition(dhs: DrainholeSpotCondition): DrainholeSpotConditionWrapper{
    return DrainholeSpotConditionWrapper(
        id = dhs.id,
        interventionId = dhs.interventionId,
        bladeId = dhs.bladeId,
        severityId = dhs.severityId,
        description = dhs.description,
    )
}