-dontobfuscate

-keep public class me.echeung.moemoekyun.client.api.socket.response.**
-keep public class me.echeung.moemoekyun.client.model.Event

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
