package org.maravill.springbootbannerplugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import org.maravill.springbootbannerplugin.ui.BannerDialog
import java.io.File
import java.io.OutputStreamWriter

class GenerateBannerAction : AnAction("Generate Spring Boot Banner") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Obtener el módulo desde el evento
        val module = e.getData(LangDataKeys.MODULE) ?: return

        // Obtener la ruta del módulo usando ModuleRootManager
        val moduleDir = ModuleRootManager.getInstance(module).contentRoots.firstOrNull()?.path
            ?: return Messages.showErrorDialog(project, "Could not find content root for the module", "Error")

        // Construir la ruta a src/main/resources
        val resourcesDir = File(moduleDir, "src/main/resources")
        if (!resourcesDir.exists()) {
            return Messages.showErrorDialog(project, "Could not find src/main/resources in the module\n Please, Right-click from 'src' folder of the project or module", "Error")
        }

        val dialog = BannerDialog(project)
        if (dialog.showAndGet()) {
            val bannerText = dialog.getBannerText()
            try {
                WriteCommandAction.runWriteCommandAction(project) {
                    val bannerFile = findOrCreateBannerFile(resourcesDir)
                    OutputStreamWriter(bannerFile.outputStream()).use {
                        it.write(bannerText)
                    }
                    val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(bannerFile)
                    virtualFile?.refresh(true, false)
                }
                Messages.showInfoMessage(project, "Banner generated successfully", "Success")
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Error creating banner.txt:\n${ex.localizedMessage}", "Error")
            }
        }
    }

    private fun findOrCreateBannerFile(resourcesDir: File): File {
        val bannerFile = File(resourcesDir, "banner.txt")
        if (!bannerFile.exists()) {
            bannerFile.createNewFile()
        }
        return bannerFile
    }
}