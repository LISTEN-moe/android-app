-dontobfuscate

-keep public class me.echeung.moemoekyun.client.api.socket.response.**
-keep public class me.echeung.moemoekyun.client.model.Event

# Google Cast
-keep class androidx.mediarouter.app.MediaRouteActionProvider { public <init>(...); }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
