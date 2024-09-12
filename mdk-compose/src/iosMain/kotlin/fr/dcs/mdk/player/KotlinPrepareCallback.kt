package fr.dcs.mdk.player

import cocoapods.mdk.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*

internal class KotlinPrepareCallback(val player: CPointer<mdkPlayerAPI>) : CompletableDeferred<KotlinPrepareResult> by CompletableDeferred(null)