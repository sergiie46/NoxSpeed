package com.noxforgestudios.noxspeed.monetization

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class ConsentManager(context: Context) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    val canRequestAds: Boolean get() = consentInformation.canRequestAds()
    val privacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gather(activity: Activity, onComplete: (FormError?) -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    onComplete(formError)
                }
            },
            { requestError -> onComplete(requestError) },
        )
    }

    fun showPrivacyOptions(activity: Activity, onComplete: (FormError?) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error -> onComplete(error) }
    }
}
