package ru.kazantsev.nsmp.sdk.sources_sync.service.utils

import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcType
import ru.kazantsev.nsmp.sdk.sources_sync.data.dto.RemoteInfo
import ru.kazantsev.nsmp.sdk.sources_sync.data.dto.RemoteInfoRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.root.SrcSetRoot
import ru.kazantsev.nsmp.sdk.sources_sync.data.src.SrcChecksum

class ConversionService {

    private fun mapChecksums(list: List<ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.SrcChecksum>): Set<SrcChecksum> {
        return list.map { SrcChecksum(code = it.code, checksum = it.checksum) }.toSet()
    }

    private fun mapRemoteInfo(list: List<RemoteInfo>): Set<SrcChecksum> {
        return list.map { SrcChecksum(code = it.code, checksum = it.checksum) }.toSet()
    }

    fun convertChecksums(scriptChecksums: ScriptChecksums): SrcSetRoot<SrcChecksum> {
        return SrcSetRoot.byEnumIterator {
            when (it) {
                SrcType.SCRIPT -> mapChecksums(scriptChecksums.scripts)
                SrcType.MODULE -> mapChecksums(scriptChecksums.modules)
                SrcType.ADV_IMPORT -> mapChecksums(scriptChecksums.advimports)
            }
        }
    }

    fun convertRemoteInfoRoot(remoteInfoRoot : RemoteInfoRoot): SrcSetRoot<SrcChecksum> {
        return SrcSetRoot.byEnumIterator {
            when (it) {
                SrcType.SCRIPT -> mapRemoteInfo(remoteInfoRoot.scripts)
                SrcType.MODULE -> mapRemoteInfo(remoteInfoRoot.modules)
                SrcType.ADV_IMPORT -> mapRemoteInfo(remoteInfoRoot.advImports)
            }
        }
    }
}