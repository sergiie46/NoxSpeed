# NoxSpeed: SDKs ship consumer rules; keep Room database metadata.
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn org.conscrypt.**
