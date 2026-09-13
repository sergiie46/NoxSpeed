package com.noxforgestudios.noxspeed.ui

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

fun requestInAppReview(activity: Activity, onAttempted: () -> Unit) {
    val manager = ReviewManagerFactory.create(activity)
    manager.requestReviewFlow().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            manager.launchReviewFlow(activity, task.result).addOnCompleteListener { onAttempted() }
        } else {
            onAttempted()
        }
    }
}
