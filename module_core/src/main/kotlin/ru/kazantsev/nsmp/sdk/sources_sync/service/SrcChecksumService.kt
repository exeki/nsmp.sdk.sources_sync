package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.set.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.simple.ISrcCodeChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair

/**
 * Сервис для сравнения чексумм локальных и удалённых исходников
 */
class SrcChecksumService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun <T : ISrcCodeChecksum, K : ISrcCodeChecksum> compareSrcSets(
        local: SrcSet<T>,
        remote: SrcSet<K>
    ): SrcSet<SrcSyncCheckPair<T, K>> {
        if (remote.type != local.type) throw IllegalArgumentException("Cannot pair src sets with different types")
        return local.convert { SrcSyncCheckPair(code = it.code, remote = remote.getByCode(it.code), local = it) }
    }

    fun <T : ISrcCodeChecksum, K : ISrcCodeChecksum> compareSrcSetRoots(
        localRoot: SrcSetRoot<T>,
        remoteRoot: SrcSetRoot<K>
    ): SrcSetRoot<SrcSyncCheckPair<T, K>> {
        val pairsRoot = SrcSetRoot(
            scripts = compareSrcSets(localRoot.scripts, remoteRoot.scripts),
            modules = compareSrcSets(localRoot.modules, remoteRoot.modules),
            advImports = compareSrcSets(localRoot.advImports, remoteRoot.advImports)
        )
        log.debug(
            "Checksum compare completed: diff: scripts={}, modules={}, advImports={}",
            pairsRoot.scripts.size,
            pairsRoot.modules.size,
            pairsRoot.advImports.size
        )
        return pairsRoot
    }
}
