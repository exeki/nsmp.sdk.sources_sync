package ru.kazantsev.nsmp.sdk.sources_sync.service.utils

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.local.ILocalSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.remote.IRemoteSrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalFileInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.pair.SrcSyncCheckPair

/**
 * Сервис для сравнения чексумм локальных и удалённых исходников
 */
class ComparisonService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun <T : ILocalFile> uniteLocalFileInfoSrcSetRoot(
        fileRoot: SrcSetRoot<T>,
        infoRoot: SrcSetRoot<LocalInfo>
    ): SrcSetRoot<LocalFileInfo> {
        return SrcSetRoot(
            scripts = uniteLocalFileInfoSrcSet(fileRoot.scripts, infoRoot.scripts),
            modules = uniteLocalFileInfoSrcSet(fileRoot.modules, infoRoot.modules),
            advImports = uniteLocalFileInfoSrcSet(fileRoot.advImports, infoRoot.advImports),
        )
    }

    fun <T : ILocalFile> uniteLocalFileInfoSrcSet(
        fileSrcSet: SrcSet<T>,
        infoSrcSet: SrcSet<LocalInfo>
    ) :  SrcSet<LocalFileInfo> {
        return fileSrcSet.convert{
            LocalFileInfo(
                file = it.file,
                code = it.code,
                info = infoSrcSet.getByCode(it.code)
            )
        }
    }

    fun <L : ILocalSrc, R : IRemoteSrc> pairSrcSets(
        local: SrcSet<L>,
        remote: SrcSet<R>
    ): SrcSet<SrcPair<L, R>> {
        if (remote.type != local.type) throw IllegalArgumentException("Cannot pair src sets with different types")
        return local.convert { SrcPair(code = it.code, remote = remote.getByCode(it.code), local = it) }
    }

    fun <L : ILocalSrc, R : IRemoteSrc> pairSrcSetRoots(
        localSrcSetRoot: SrcSetRoot<L>,
        remoteSrcSetRoot: SrcSetRoot<R>
    ): SrcSetRoot<SrcPair<L, R>> {
        val pairsRoot = SrcSetRoot(
            scripts = pairSrcSets(localSrcSetRoot.scripts, remoteSrcSetRoot.scripts),
            modules = pairSrcSets(localSrcSetRoot.modules, remoteSrcSetRoot.modules),
            advImports = pairSrcSets(localSrcSetRoot.advImports, remoteSrcSetRoot.advImports)
        )
        log.debug(
            "Checksum compare completed: diff: scripts={}, modules={}, advImports={}",
            pairsRoot.scripts.size,
            pairsRoot.modules.size,
            pairsRoot.advImports.size
        )
        return pairsRoot
    }

    fun <L : ILocalChecksum, R : IRemoteChecksum> pairSyncCheck(
        localSrcSetRoot: SrcSetRoot<L>,
        remoteSrcSetRoot: SrcSetRoot<R>
    ): SrcSetRoot<SrcSyncCheckPair<L, R>> {
        val pairs = pairSrcSetRoots(localSrcSetRoot, remoteSrcSetRoot)
        return pairs.convert {
            SrcSyncCheckPair(
                code = it.code,
                remote = it.remote,
                local = it.local
            )
        }
    }
}
