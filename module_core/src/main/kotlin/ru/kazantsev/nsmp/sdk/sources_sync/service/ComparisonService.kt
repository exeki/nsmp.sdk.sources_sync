package ru.kazantsev.nsmp.sdk.sources_sync.service

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair

/**
 * Сервис для сравнения чексумм локальных и удалённых исходников
 */
class ComparisonService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun <L : ILocalSrc, R : IRemoteSrc> compareSrcSets(
        local: SrcSet<L>,
        remote: SrcSet<R>
    ): SrcSet<SrcPair<L, R>> {
        if (remote.type != local.type) throw IllegalArgumentException("Cannot pair src sets with different types")
        return local.convert { SrcPair(code = it.code, remote = remote.getByCode(it.code), local = it) }
    }

    fun <L : ILocalSrc, R : IRemoteSrc> compareSrcSetRoots(
        localRoot: SrcSetRoot<L>,
        remoteRoot: SrcSetRoot<R>
    ): SrcSetRoot<SrcPair<L, R>> {
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

    fun <L : ILocalChecksum, R : IRemoteChecksum> compareSyncCheck(
        localRoot: SrcSetRoot<L>,
        remoteRoot: SrcSetRoot<R>
    ) : SrcSetRoot<SrcSyncCheckPair<L, R>>{
        val pairs = compareSrcSetRoots(localRoot, remoteRoot)
        return pairs.convert {
            SrcSyncCheckPair(
                code = it.code,
                remote = it.remote,
                local = it.local
            )
        }
    }
}
