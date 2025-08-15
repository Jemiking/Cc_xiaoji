# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep application classes
-keep class com.ccxiaoji.app.** { *; }

# Keep all provide methods in modules
-keepclassmembers class * {
    @dagger.Provides *;
    @dagger.hilt.android.* *;
    @dagger.Module *;
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.ccxiaoji.app.**$$serializer { *; }
-keepclassmembers class com.ccxiaoji.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.ccxiaoji.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep class com.ccxiaoji.app.data.local.entity.** { *; }
-keep class com.ccxiaoji.app.data.remote.dto.** { *; }
-keep class com.ccxiaoji.app.domain.model.** { *; }

# Hilt - Enhanced rules
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keepclasseswithmembernames class * {
    @dagger.hilt.* <fields>;
}
-keepclasseswithmembernames class * {
    @dagger.hilt.* <methods>;
}
-keep class dagger.hilt.internal.** { *; }
-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    @javax.inject.Inject <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager.FragmentContextWrapper { *; }
-keepclassmembers class * {
    @dagger.hilt.android.qualifiers.ApplicationContext *;
    @dagger.hilt.android.qualifiers.ActivityContext *;
}
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
}
-keepclassmembers class * {
    @dagger.* <fields>;
}
-keepclassmembers class * {
    @dagger.* <methods>;
}
-dontwarn dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories*

# Hilt Workers
-keep class * extends androidx.work.Worker {
    @androidx.hilt.work.HiltWorker <init>(...);
}
-keep class * extends androidx.work.ListenableWorker {
    @androidx.hilt.work.HiltWorker <init>(...);
}
-keep class androidx.hilt.work.** { *; }

# Keep all modules and generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Impl { *; }
-keep class **_Impl$* { *; }
-keep class **_Factory { *; }
-keep class **_Factory$* { *; }
-keep class **_GeneratedInjector { *; }
-keep class **_MembersInjector { *; }
-keep class **_ProvideFactory { *; }
-keep class **_Provide { *; }

# Keep Dagger/Hilt generated classes
-keep class **$$ModuleAdapter { *; }
-keep class **$$InjectAdapter { *; }
-keep class **$$Factory { *; }
-keep class dagger.internal.** { *; }

# Keep all ViewModels from feature modules
-keep class com.ccxiaoji.feature.**.viewmodel.** extends androidx.lifecycle.ViewModel { *; }
-keep class com.ccxiaoji.feature.**.presentation.viewmodel.** extends androidx.lifecycle.ViewModel { *; }

# Keep all APIs from feature modules
-keep interface com.ccxiaoji.feature.**.api.** { *; }
-keep class * implements com.ccxiaoji.feature.**.api.** { *; }

# Keep all Retrofit service interfaces
-keep interface com.ccxiaoji.**.service.** { *; }
-keep interface com.ccxiaoji.**.api.** { *; }
-keep interface com.ccxiaoji.**.remote.** { *; }

# Keep AuthApi and related classes
-keep interface com.ccxiaoji.**.AuthApi { *; }
-keep class com.ccxiaoji.**.AuthApiImpl { *; }
-keep class com.ccxiaoji.**.auth.** { *; }

# Keep all API implementations
-keep class com.ccxiaoji.**.*ApiImpl { *; }
-keep class com.ccxiaoji.**.*Api { *; }

# Keep entities from all modules
-keep class com.ccxiaoji.**.entity.** { *; }
-keep class com.ccxiaoji.**.model.** { *; }
-keep class com.ccxiaoji.**.dto.** { *; }

# Keep all data classes and their members
-keepclassmembers class com.ccxiaoji.** {
    <fields>;
    <init>(...);
    <methods>;
}

# Keep all Retrofit related classes
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}

# Keep network module classes
-keep class com.ccxiaoji.network.** { *; }
-keep class com.ccxiaoji.core.network.** { *; }

# Prevent stripping of methods/fields annotated with specific annotations
-keepclassmembers class * {
    @javax.inject.* *;
    @dagger.* *;
    @dagger.hilt.* *;
}

# FastExcel rules
-dontwarn javax.xml.stream.**
-dontwarn org.codehaus.stax2.**
-dontwarn com.fasterxml.aalto.**
-keep class org.dhatim.fastexcel.** { *; }
-keep class com.fasterxml.aalto.** { *; }
-keep class org.codehaus.stax2.** { *; }

# Ignore missing XML classes that FastExcel references but aren't used on Android
-ignorewarnings

# 保留日志输出 - 不要移除Log调用
# 注意：-assumenosideeffects会移除这些调用，所以我们不使用它
# 如果需要在发布版本中移除日志，请取消下面的注释
# -assumenosideeffects class android.util.Log {
#     public static int v(...);
#     public static int d(...);
# }

# 保留所有日志类
-keep class android.util.Log { *; }
-keep class java.io.PrintStream { *; }

