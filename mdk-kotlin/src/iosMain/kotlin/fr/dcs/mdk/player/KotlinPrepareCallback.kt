package fr.dcs.mdk.player

import cocoapods.mdk.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*

@OptIn(ExperimentalForeignApi::class)
internal class KotlinPrepareCallback(val player: CPointer<mdkPlayerAPI>) : CompletableDeferred<KotlinPrepareResult> by CompletableDeferred(null)