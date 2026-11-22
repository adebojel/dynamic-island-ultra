package com.adebojel.dynamicislandultra.floating

data class UnitOption(val id: String, val label: String, val factorToBase: Double = 1.0)
data class UnitGroup(val key: String, val label: String, val units: List<UnitOption>)

/** Pure local unit conversion with no network, sensor, or permission dependency. */
object LocalUnitConverter {
    val groups = listOf(
        UnitGroup("length", "Panjang", listOf(
            UnitOption("mm", "Milimeter (mm)", 0.001),
            UnitOption("cm", "Sentimeter (cm)", 0.01),
            UnitOption("m", "Meter (m)", 1.0),
            UnitOption("km", "Kilometer (km)", 1000.0),
            UnitOption("in", "Inci (in)", 0.0254),
            UnitOption("ft", "Kaki (ft)", 0.3048)
        )),
        UnitGroup("mass", "Massa", listOf(
            UnitOption("mg", "Miligram (mg)", 0.001),
            UnitOption("g", "Gram (g)", 1.0),
            UnitOption("kg", "Kilogram (kg)", 1000.0),
            UnitOption("lb", "Pound (lb)", 453.59237),
            UnitOption("oz", "Ounce (oz)", 28.349523125)
        )),
        UnitGroup("volume", "Volume", listOf(
            UnitOption("ml", "Mililiter (mL)", 1.0),
            UnitOption("l", "Liter (L)", 1000.0),
            UnitOption("tsp", "Sendok teh (US tsp)", 4.92892159375),
            UnitOption("tbsp", "Sendok makan (US tbsp)", 14.78676478125),
            UnitOption("cup", "Cangkir (US cup)", 236.5882365)
        )),
        UnitGroup("temperature", "Suhu", listOf(
            UnitOption("c", "Celsius (°C)"),
            UnitOption("f", "Fahrenheit (°F)"),
            UnitOption("k", "Kelvin (K)")
        ))
    )

    fun group(key: String): UnitGroup = groups.firstOrNull { it.key == key } ?: groups.first()

    fun convert(value: Double, groupKey: String, fromId: String, toId: String): Double? {
        if (!value.isFinite()) return null
        val selectedGroup = groups.firstOrNull { it.key == groupKey } ?: return null
        if (selectedGroup.units.none { it.id == fromId } || selectedGroup.units.none { it.id == toId }) return null
        if (groupKey == "temperature") {
            val celsius = when (fromId) {
                "c" -> value
                "f" -> (value - 32.0) * 5.0 / 9.0
                "k" -> value - 273.15
                else -> return null
            }
            return when (toId) {
                "c" -> celsius
                "f" -> celsius * 9.0 / 5.0 + 32.0
                "k" -> celsius + 273.15
                else -> null
            }
        }
        val from = selectedGroup.units.first { it.id == fromId }
        val to = selectedGroup.units.first { it.id == toId }
        return value * from.factorToBase / to.factorToBase
    }

    fun format(value: Double): String {
        if (!value.isFinite()) return "Tidak valid"
        return "%.6f".format(java.util.Locale.ROOT, value).trimEnd('0').trimEnd('.').ifBlank { "0" }
    }
}
