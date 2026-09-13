package com.noxforgestudios.noxspeed

import android.app.Application
import androidx.room.Room
import com.noxforgestudios.noxspeed.data.db.NoxSpeedDatabase
import com.noxforgestudios.noxspeed.data.db.TripRepository
import com.noxforgestudios.noxspeed.data.prefs.SettingsRepository
import com.noxforgestudios.noxspeed.monetization.AdManager
import com.noxforgestudios.noxspeed.monetization.BillingManager
import com.noxforgestudios.noxspeed.monetization.ConsentManager

class NoxSpeedApplication : Application() {
    lateinit var database: NoxSpeedDatabase
        private set
    lateinit var trips: TripRepository
        private set
    lateinit var settings: SettingsRepository
        private set
    lateinit var billing: BillingManager
        private set
    lateinit var ads: AdManager
        private set
    lateinit var consent: ConsentManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, NoxSpeedDatabase::class.java, "noxspeed.db")
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
        trips = TripRepository(database.tripDao(), database.accelerationDao())
        settings = SettingsRepository(this)
        billing = BillingManager(this, settings)
        consent = ConsentManager(this)
        ads = AdManager(this)
        billing.connect()
    }
}
