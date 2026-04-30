package ru.kazantsev.nsmp.sdk.sources_sync.service.utils

import org.slf4j.LoggerFactory
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.sets.SrcSet
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrc
import ru.kazantsev.nsmp.sdk.sources_sync.data.signature.src.ISrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcFileChecksum
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcPair
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcSyncCheckPair

/**
 * Сервис для сравнения чексумм локальных и удалённых исходников
 */
class ComparisonService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun <T : ISrcFile, C : ISrcChecksum> uniteFileChecksumSrcSetRoots(
        fileRoot: SrcSetRoot<T>,
        infoRoot: SrcSetRoot<C>
    ): SrcSetRoot<SrcFileChecksum> {
        return SrcSetRoot.byEnumIterator { uniteFileChecksumSrcSets(fileRoot[it], infoRoot[it]) }
    }

    fun <T : ISrcFile, C : ISrcChecksum> uniteFileChecksumSrcSets(
        fileSrcSet: SrcSet<T>,
        infoSrcSet: SrcSet<C>
    ): SrcSet<SrcFileChecksum> {
        return fileSrcSet.convert {
            val info = infoSrcSet.getByCode(it.code)
            SrcFileChecksum(
                file = it.file,
                code = it.code,
                checksum = info?.checksum
            )
        }
    }

    fun <L : ISrc, R : ISrc> pairSrcSets(
        left: SrcSet<L>,
        right: SrcSet<R>
    ): SrcSet<SrcPair<L, R>> {
        if (right.type != left.type) throw IllegalArgumentException("Cannot pair src sets with different types")
        return left.convert { SrcPair(code = it.code, right = right.getByCode(it.code), left = it) }
    }

    fun <L : ISrc, R : ISrc> pairSrcSetRoots(
        leftSrcSetRoot: SrcSetRoot<L>,
        rightSrcSetRoot: SrcSetRoot<R>
    ): SrcSetRoot<SrcPair<L, R>> {
        val pairsRoot = SrcSetRoot.byEnumIterator { pairSrcSets(leftSrcSetRoot[it], rightSrcSetRoot[it]) }
        log.debug(
            "Checksum compare completed: diff: scripts={}, modules={}, advImports={}",
            pairsRoot.scripts.size,
            pairsRoot.modules.size,
            pairsRoot.advImports.size
        )
        return pairsRoot
    }

    fun <L : ISrcChecksum, R : ISrcChecksum> pairSyncCheck(
        leftSrcSetRoot: SrcSetRoot<L>,
        rightSrcSetRoot: SrcSetRoot<R>
    ): SrcSetRoot<SrcSyncCheckPair<L, R>> {
        val pairs = pairSrcSetRoots(leftSrcSetRoot, rightSrcSetRoot)
        return pairs.convert { _, pair ->
            SrcSyncCheckPair(
                code = pair.code,
                right = pair.right,
                left = pair.left
            )
        }
    }
}
