package fr.dcs.mdk.utils

import cocoapods.mdk.*
import fr.dcs.mdk.native.*
import kotlinx.cinterop.*
import platform.posix.*

internal fun NativeMediaInfo.Companion.fromC(value: mdkMediaInfo?): NativeMediaInfo? {
  if (value == null) return null
  return memScoped {
    NativeMediaInfo(
      startTime = value.start_time,
      duration = value.duration,
      bitRate = value.bit_rate,
      size = value.size,
      format = value.format?.toKStringFromUtf8().orEmpty(),
      streams = value.streams,
      audio = buildList {
        val audioTracks = value.audio ?: return@buildList
        for (i in 0 until value.nb_audio) {
          val element = audioTracks[i]

          val codec = alloc<mdkAudioCodecParameters>().apply { MDK_AudioStreamCodecParameters(element.ptr, this.ptr) }

          add(
            NativeAudioStream(
              index = element.index,
              startTime = element.start_time,
              duration = element.duration,
              frames = element.frames,
              metaData = buildMap {
                val entry = alloc<mdkStringMapEntry>()
                while (MDK_AudioStreamMetadata(element.ptr, entry.ptr)) {
                  val mapKey = entry.key?.toKStringFromUtf8() ?: continue
                  val mapValue = entry.value?.toKStringFromUtf8() ?: continue
                  put(mapKey, mapValue)
                }
              },
              codec = NativeAudioCodec(
                codec = codec.codec?.toKStringFromUtf8().orEmpty(),
                codecTag = codec.codec_tag.toInt(),
                bitRate = codec.bit_rate,
                profile = codec.profile,
                level = codec.level,
                frameRate = codec.frame_rate,
                isFloat = codec.is_float,
                isUnsigned = codec.is_unsigned,
                isPlanar = codec.is_planar,
                rawSampleSize = codec.raw_sample_size,
                channels = codec.channels,
                sampleRate = codec.channels,
                blockAlign = codec.block_align,
                frameSize = codec.frame_size,
              ),
            )
          )
        }
      },
      video = buildList {
        val videoTracks = value.video ?: return@buildList
        for (i in 0 until value.nb_video) {
          val element = videoTracks[i]
          val codec = alloc<mdkVideoCodecParameters>().apply { MDK_VideoStreamCodecParameters(element.ptr, this.ptr) }
          add(
            NativeVideoStream(
              index = element.index,
              startTime = element.start_time,
              duration = element.duration,
              frames = element.frames,
              rotation = element.rotation,
              codec = NativeVideoCodec(
                codec = codec.codec?.toKStringFromUtf8().orEmpty(),
                codecTag = codec.codec_tag.toInt(),
                bitRate = codec.bit_rate,
                profile = codec.profile,
                level = codec.level,
                frameRate = codec.frame_rate,
                format = codec.format,
                formatName = codec.format_name?.toKStringFromUtf8().orEmpty(),
                width = codec.width,
                height = codec.height,
                bFrames = codec.b_frames,
                par = codec.par,
              ),
              metaData = buildMap {
                val entry = alloc<mdkStringMapEntry>()
                while (MDK_VideoStreamMetadata(element.ptr, entry.ptr)) {
                  val mapKey = entry.key?.toKStringFromUtf8() ?: continue
                  val mapValue = entry.value?.toKStringFromUtf8() ?: continue
                  put(mapKey, mapValue)
                }
              }
            )
          )
        }
      },
      subtitles = buildList {
        val audioTracks = value.subtitle ?: return@buildList
        for (i in 0 until value.nb_subtitle) {
          val element = audioTracks[i]
          val codec = alloc<mdkSubtitleCodecParameters>().apply { MDK_SubtitleStreamCodecParameters(element.ptr, this.ptr) }
          add(
            NativeSubtitle(
              index = element.index,
              startTime = element.start_time,
              duration = element.duration,
              metaData = buildMap {
                val entry = alloc<mdkStringMapEntry>()
                while (MDK_SubtitleStreamMetadata(element.ptr, entry.ptr)) {
                  val mapKey = entry.key?.toKStringFromUtf8() ?: continue
                  val mapValue = entry.value?.toKStringFromUtf8() ?: continue
                  put(mapKey, mapValue)
                }
              },
              codec = NativeSubtitleCodec(
                codec = codec.codec?.toKStringFromUtf8().orEmpty(),
                codecTag = codec.codec_tag.toInt(),
              ),
            )
          )
        }
      },
    )
  }
}