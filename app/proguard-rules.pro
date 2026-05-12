# ============================================================
# LightSpeed Browser — ProGuard / R8 Optimization Rules
# Aggressive optimization for minimal APK size
# ============================================================

# ── Kotlin ──────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlin.**

# Keep coroutines internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── WebView ─────────────────────────────────────────────────
# Keep JavaScript interface methods accessible from WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView class
-keep class android.webkit.** { *; }

# ── Room ────────────────────────────────────────────────────
# Keep Room entities (serialized/deserialized by Room)
-keep class com.lightspeed.browser.data.db.entities.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Room uses reflection for DAO implementations
-keep class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}

# ── Application ─────────────────────────────────────────────
# Keep our application class
-keep class com.lightspeed.browser.BrowserApplication { *; }

# ── Aggressive Optimization ─────────────────────────────────
# These are safe because we don't use reflection extensively
-repackageclasses 'com.lightspeed.browser'
-allowaccessmodification
-mergeinterfacesaggressively

# Remove all logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# Remove Kotlin null checks in release
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(...);
    static void checkExpressionValueIsNotNull(...);
    static void checkNotNullExpressionValue(...);
    static void checkReturnedValueIsNotNull(...);
    static void checkFieldIsNotNull(...);
}

# Remove Kotlin debug info
-assumenosideeffects class kotlin.jvm.internal.SourceDebugExtension {
    void init(...);
}

# ── AndroidX ────────────────────────────────────────────────
-keep class androidx.appcompat.** { *; }
-keep interface androidx.appcompat.** { *; }
-dontwarn androidx.**

# ── Miscellaneous ───────────────────────────────────────────
# Keep enum classes (used in when() statements)
-keepclassmembers enum * { *; }

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
