package com.heliopales.bladeexpertfiller.damages

enum class DamageTypeCategory(val code:String, val alias:String) {

    AER( "AER", "Aerodynamic elements" ),
    BDL( "BDL", "Bonding line" ),
    FTS( "FTS", "Features" ),
    LAM( "LAM", "Laminate" ),
    LEP( "LEP", "Leading edge protection" ),
    LGH( "LGH", "Lighting damage" ),
    LPS( "LPS", "Lighting protection" ),
    NDT( "NDT", "Non destructive testing" ),
    SF( "SF", "Surface" ),
    VIEW( "VW", "View" ),
    WEB( "SWB", "Shear-web" ),
    OTHER( "ZZZ", "Other" );

    override fun toString(): String {
        return  "${super.toString()} - $alias"
    }

}