package com.github.killaxy.shopwarehelper.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import java.io.File


class MyProjectAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val fileEditorManager = FileEditorManager.getInstance(project)

        if (editor != null) {
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
            val templateToCopyPath = psiFile.virtualFile.path
            val projectPath = project.basePath
            val src = File("$projectPath/src")
            val relativePath = templateToCopyPath.substring(templateToCopyPath.indexOf("views") + "views".length)
            val srcPath = findSrcPath(src)
            if (srcPath == "") return

            val pathToSrcViewsFolder = ("$srcPath\\Resources\\views$relativePath").replace("/", "\\")
            val file = File(pathToSrcViewsFolder)
            file.parentFile.mkdirs()
            if (!file.createNewFile()) return
            file.writeText("{% sw_extends '@Storefront$relativePath' %}\n")

            openFile(file, fileEditorManager, project)
        }
    }

    private fun findSrcPath(src: File): String {
        src.walk().first().listFiles()[0]
        var srcPath = ""
        for (file in src.walk().first().listFiles()) {
            if (file.isDirectory) {
                srcPath = file.path
                break
            }
        }
        return srcPath
    }

    private fun openFile(
        file: File,
        fileEditorManager: FileEditorManager,
        project: Project
    ) {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: return

        fileEditorManager.openTextEditor(
            OpenFileDescriptor(
                project,
                virtualFile,
                1,
                0
            ),
            true
        )
    }
}