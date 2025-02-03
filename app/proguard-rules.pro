-keep class * { *; }

-keepclassmembers class * { public protected *; }

-keepclasseswithmembernames class * { native <methods>; }

-keepattributes *Annotation*

-dontwarn org.slf4j.**

-dontwarn sun.font.**

-dontwarn android.util.Log
-dontwarn androidx.lifecycle.viewmodel.internal.JvmViewModelProviders
-dontwarn androidx.savedstate.Recreator
-dontwarn androidx.savedstate.SavedStateRegistry
-dontwarn io.ktor.network.sockets.UnixSocketAddress
-dontwarn kotlinx.serialization.internal.PlatformKt

-ignorewarnings