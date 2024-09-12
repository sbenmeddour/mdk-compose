package fr.dcs.mdk.base

expect object MdkLibrary {
  fun initialize(configuration: Configuration)
  val options: Options
}

interface Options {
  fun getString(key: String): String?
  fun getInt(key: String): Int?

  operator fun set(key: String, value: String?)
  operator fun set(key: String, value: Int)
  operator fun set(key: String, value: Float)
}