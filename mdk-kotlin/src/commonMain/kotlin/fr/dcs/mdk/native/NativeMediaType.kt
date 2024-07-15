package fr.dcs.mdk.native

internal enum class NativeMediaType(val nativeValue: Int)  {

  Unknown(-1),
  Video(0),
  Audio(1),
  Subtitles(3),
}