package net.ntworld.intellijCodeCleaner.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import javax.swing.Icon

class AnalyzeAction(text: String?, description: String?, icon: Icon?) : AnAction(text, description, icon) {
    constructor(): this(null, null, null)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (null !== project) {
            ProgressManager.getInstance().run(AnalyzeTask(project))
        }
    }

}