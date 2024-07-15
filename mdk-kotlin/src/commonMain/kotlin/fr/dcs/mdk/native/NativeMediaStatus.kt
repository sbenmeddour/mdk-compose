package fr.dcs.mdk.native

enum class NativeMediaStatus(val nativeValue: Int) {
  NoMedia(0),
  Unloaded(1),
  Loading(1 shl 1),
  Loaded(1 shl 2),
  Prepared(1 shl 8),
  Stalled(1 shl 3),
  Buffering(1 shl 4),
  Buffered(1 shl 5),
  End(1 shl 6),
  Seeking(1 shl 7),
  Invalid(1 shl 31);

  internal infix fun and(other: Int): Boolean = this.nativeValue and other != 0

}

