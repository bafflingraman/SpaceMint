# SpaceMint ProGuard Rules

# Keep app classes
-keep class com.spacemint.app.** { *; }

# Keep Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class **$WhenMappings { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep notification classes
-keep class * extends android.content.BroadcastReceiver { *; }

# Keep MediaStore
-keep class android.provider.MediaStore { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}