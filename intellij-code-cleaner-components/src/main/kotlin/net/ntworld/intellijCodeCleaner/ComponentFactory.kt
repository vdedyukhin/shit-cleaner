package net.ntworld.intellijCodeCleaner

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.wm.ToolWindow
import net.ntworld.codeCleaner.structure.MaintainabilityRate
import net.ntworld.foundation.Infrastructure
import net.ntworld.intellijCodeCleaner.component.annotation.AnnotationGutterDataFactory
import net.ntworld.intellijCodeCleaner.component.annotation.AnnotationManager
import net.ntworld.intellijCodeCleaner.component.button.MaintainabilityFilterButton
import net.ntworld.intellijCodeCleaner.component.button.AnalyzeButton
import net.ntworld.intellijCodeCleaner.component.button.AnnotationToggleButton
import net.ntworld.intellijCodeCleaner.component.button.StopButton
import net.ntworld.intellijCodeCleaner.component.codeSmells.CodeSmellsTab
import net.ntworld.intellijCodeCleaner.component.duplications.DuplicationsTab
import net.ntworld.intellijCodeCleaner.component.overview.OverviewTab
import net.ntworld.intellijCodeCleaner.component.toolbar.MainToolbar
import net.ntworld.redux.Dispatcher

interface ComponentFactory {

    fun makeInfrastructure(): Infrastructure

    fun findDispatcher(projectId: String): Dispatcher<AppStore>

    fun makeDispatcher(ideaProject: IdeaProject): Dispatcher<AppStore>

    fun makeAnalyzeButton(): AnalyzeButton

    fun makeAnnotationToggleButton(): AnnotationToggleButton

    fun makeMaintainabilityFilterButton(rate: MaintainabilityRate): MaintainabilityFilterButton

    fun makeStopButton(): StopButton

    fun makeMainToolbar(): MainToolbar

    fun makeOverviewTab(ideaProject: IdeaProject, toolWindow: ToolWindow): OverviewTab

    fun makeCodeSmellsTab(ideaProject: IdeaProject, toolWindow: ToolWindow): CodeSmellsTab

    fun makeDuplicationsTab(ideaProject: IdeaProject, toolWindow: ToolWindow): DuplicationsTab

    fun makeAnnotationManager(ideaProject: IdeaProject): AnnotationManager

    fun makeAnnotationGutterDataFactory(ideaProject: IdeaProject): AnnotationGutterDataFactory

    fun useDispatcherOf(ideaProject: IdeaProject?, block: Dispatcher<AppStore>.() -> Unit) {
        if (null !== ideaProject) {
            block.invoke(this.makeDispatcher(ideaProject))
        }
    }
}