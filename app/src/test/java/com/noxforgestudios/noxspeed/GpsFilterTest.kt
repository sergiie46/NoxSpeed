package com.noxforgestudios.noxspeed

import com.noxforgestudios.noxspeed.data.prefs.GpsFilterMode
import com.noxforgestudios.noxspeed.gps.GpsFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsFilterTest {
    @Test fun stationaryNoiseSettlesAtZero() {
        val filter = GpsFilter()
        assertEquals(0.0, filter.filter(0.2, 5f, 1000, GpsFilterMode.BALANCED), 0.01)
        assertEquals(0.0, filter.filter(0.3, 5f, 2000, GpsFilterMode.BALANCED), 0.01)
    }

    @Test fun impossibleSpikeIsRejectedWhenAccuracyIsPoor() {
        val filter = GpsFilter()
        val base = filter.filter(10.0, 5f, 1000, GpsFilterMode.BALANCED)
        val spike = filter.filter(80.0, 20f, 1100, GpsFilterMode.BALANCED)
        assertEquals(base, spike, 0.01)
    }

    @Test fun smoothModeConvergesWithoutOvershoot() {
        val filter = GpsFilter()
        filter.filter(0.0, 4f, 1000, GpsFilterMode.SMOOTH)
        val a = filter.filter(20.0, 4f, 2000, GpsFilterMode.SMOOTH)
        val b = filter.filter(20.0, 4f, 3000, GpsFilterMode.SMOOTH)
        assertTrue(a in 0.0..20.0)
        assertTrue(b in a..20.0)
    }
}
