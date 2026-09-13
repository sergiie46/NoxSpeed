package com.noxforgestudios.noxspeed

import com.noxforgestudios.noxspeed.data.prefs.SpeedUnit
import com.noxforgestudios.noxspeed.gps.Units
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitsTest {
    @Test fun convertsMetersPerSecond() {
        assertEquals(36.0, Units.speedFromMps(10.0, SpeedUnit.KMH), 0.001)
        assertEquals(22.369, Units.speedFromMps(10.0, SpeedUnit.MPH), 0.01)
        assertEquals(19.438, Units.speedFromMps(10.0, SpeedUnit.KNOTS), 0.01)
    }
}
