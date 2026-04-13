package ru.kazantsev.nsmp.sdk.sources_sync.dto

import ru.kazantsev.nsmp.basic_api_connector.dto.nsmp.ScriptChecksums

class SrcInfoRoot(
    val modules: List<SrcInfo> = emptyList(),
    val scripts: List<SrcInfo> = emptyList(),
    val advImports: List<SrcInfo> = emptyList()
) {
    companion object {
        fun fromChecksums(checksums: ScriptChecksums): SrcInfoRoot {
            return SrcInfoRoot(
                scripts = checksums.scripts.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
                modules = checksums.modules.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
                advImports = checksums.advimports.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
            )
        }
    }
}
