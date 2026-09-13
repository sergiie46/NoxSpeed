package com.noxforgestudios.noxspeed.ui

fun formatDuration(ms: Long): String {
    val total = (ms / 1000L).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

fun headingLabel(degrees: Float?): String {
    if (degrees == null || !degrees.isFinite()) return "—"
    val labels = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val normalized = ((degrees % 360f) + 360f) % 360f
    val index = ((normalized + 22.5f) / 45f).toInt() % 8
    return labels[index]
}
