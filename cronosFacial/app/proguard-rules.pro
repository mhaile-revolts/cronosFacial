# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all annotations
-keepattributes *Annotation*

# Keep generic signatures for Kotlin
-keepattributes Signature

# Keep exceptions
-keepattributes Exceptions

## Retrofit / OkHttp / Gson ##
# Retrofit does reflection on generic parameters
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Keep Retrofit interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Gson specific
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Keep data classes for network models
-keep class com.cronosedx.cronosfacial.network.** { *; }
-keep class com.cronosedx.cronosfacial.model.** { *; }
-keep class com.cronosedx.cronosfacial.ui.FaceSessionData { *; }

## CameraX ##
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

## ML Kit ##
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

## TensorFlow Lite ##
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

## Hilt ##
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

## Kotlin Coroutines ##
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

## Compose ##
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep all ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep repository classes
-keep class com.cronosedx.cronosfacial.repository.** { *; }

# Keep analyzer classes
-keep class com.cronosedx.cronosfacial.analyzer.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
