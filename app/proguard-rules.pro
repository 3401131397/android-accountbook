# ===== Room Database Keep Rules =====
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Database class * { *; }
-dontwarn androidx.room.paging.**

# ===== MPAndroidCharts =====
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**
