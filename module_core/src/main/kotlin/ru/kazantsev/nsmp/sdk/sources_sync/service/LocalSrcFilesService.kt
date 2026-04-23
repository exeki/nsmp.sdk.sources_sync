package ru.kazantsev.nsmp.sdk.sources_sync.service

import ru.kazantsev.nsmp.sdk.sources_sync.data.src.local.LocalSrcFile
import ru.kazantsev.nsmp.sdk.sources_sync.data.SrcRequest
import ru.kazantsev.nsmp.sdk.sources_sync.data.lookup.LookupResultRoot

class LocalSrcFilesService(
    private val scriptsSrcFolder: SrcFolder,
    private val modulesSrcFolder: SrcFolder,
    private val advImportsSrcFolder: SrcFolder,
) {
    fun findLocalFiles(req: SrcRequest): LookupResultRoot<LocalSrcFile> {
        return LookupResultRoot(
            scripts = scriptsSrcFolder.findSourceFiles(req.scripts, req.allScripts, req.scriptCodesExcluded),
            modules = modulesSrcFolder.findSourceFiles(req.modules, req.allModules, req.modulesExcluded),
            advImports = advImportsSrcFolder.findSourceFiles(req.advImports, req.allAdvImports, req.advImportsExcluded)
        )
    }
}