# Keep the saved-game classes so old saves still load after shrinking.
-keep @kotlinx.serialization.Serializable class com.lexi.christmastown.** { *; }
-keepclassmembers class com.lexi.christmastown.** { *** Companion; *** serializer(...); }
