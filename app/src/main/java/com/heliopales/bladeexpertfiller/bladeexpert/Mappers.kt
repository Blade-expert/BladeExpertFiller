package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageTypeCategory
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType
import com.heliopales.bladeexpertfiller.secondaryentities.Severity
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.LightningSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.lightning.LightningReceptor
import com.heliopales.bladeexpertfiller.spotcondition.lightning.ReceptorMeasure
import com.heliopales.bladeexpertfiller.turbine.Turbine

fun mapBladeExpertIntervention(interventionWrapper: InterventionWrapper): Intervention {
    return Intervention(
        id = interventionWrapper.id,
        name = interventionWrapper.name,
        turbineId= interventionWrapper.turbineId,
        turbineSerial = interventionWrapper.turbineSerial,
        windfarmId = interventionWrapper.windfarmId,
        windfarmName = interventionWrapper.windfarmName
    )
}

fun mapToBladeExpertIntervention(intervention: Intervention): InterventionWrapper {
    return InterventionWrapper(
        id = intervention.id,
        name = intervention.name,
        turbineId = intervention.turbineId,
        turbineSerial = intervention.turbineSerial,
        windfarmId = intervention.windfarmId,
        windfarmName = intervention.windfarmName,
        blades = null,
        turbines = null
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
        position = blade.position,
        receptors = null
    )
}

fun mapBladeExpertTurbine(turbineWrapper: TurbineWrapper): Turbine {
    return Turbine(
        id =turbineWrapper.id,
        alias = turbineWrapper.alias,
        serial = turbineWrapper.serial,
        numInWindfarm = turbineWrapper.numInWindfarm,
        windfarmId = turbineWrapper.windfarmId
    )
}

fun mapBladeExpertLightningReceptor(lightningReceptorWrapper: LightningReceptorWrapper): LightningReceptor {
    return LightningReceptor(
        id = lightningReceptorWrapper.id,
        bladeId = lightningReceptorWrapper.bladeId,
        radius = lightningReceptorWrapper.radius,
        position = lightningReceptorWrapper.position
    )
}

/*
fun mapToBladeExpertLightningReceptor(lightningReceptor: LightningReceptor): LightningReceptorWrapper {
    return LightningReceptorWrapper(
        id = lightningReceptor.id,
        bladeId = lightningReceptor.bladeId,
        radius = lightningReceptor.radius,
        position = lightningReceptor.position
    )
}
*/


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

fun mapToBladeExpertDamageSpotCondition(dsc: DamageSpotCondition): DamageSpotConditionWrapper {
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

fun mapToBladeExpertDrainholeSpotCondition(dhs: DrainholeSpotCondition): DrainholeSpotConditionWrapper {
    return DrainholeSpotConditionWrapper(
        id = dhs.id,
        interventionId = dhs.interventionId,
        bladeId = dhs.bladeId,
        severityId = dhs.severityId,
        description = dhs.description,
    )
}

fun mapToBladeExpertLightningSpotCondition(
    lsc: LightningSpotCondition,
    measures: List<LightningReceptorMeasureWrapper>
): LightningSpotConditionWrapper {
    return LightningSpotConditionWrapper(
        id = lsc.id,
        interventionId = lsc.interventionId,
        bladeId = lsc.bladeId,
        description = lsc.description,
        measures = measures,
        measureMethod = lsc.measureMethod
    )
}

fun mapToBladeExpertLightningMeasure(lrm: ReceptorMeasure): LightningReceptorMeasureWrapper {
    return LightningReceptorMeasureWrapper(
        receptorId = lrm.receptorId,
        value = if (lrm.isOverLimit) "OL" else (if(lrm.value == null) null else lrm.value.toString()),
        severityId = lrm.severityId
    )
}

