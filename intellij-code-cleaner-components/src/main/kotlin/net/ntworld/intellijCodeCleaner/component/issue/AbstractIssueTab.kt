package net.ntworld.intellijCodeCleaner.component.issue

import com.intellij.find.FindModel
import com.intellij.find.impl.FindInProjectUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.usageView.UsageInfo
import com.intellij.usages.impl.UsagePreviewPanel
import net.ntworld.codeCleaner.structure.Issue
import net.ntworld.intellijCodeCleaner.*
import net.ntworld.intellijCodeCleaner.component.issue.node.FileNode
import net.ntworld.intellijCodeCleaner.component.issue.node.MainIssueNode
import net.ntworld.intellijCodeCleaner.component.issue.node.RelatedIssueNode
import net.ntworld.intellijCodeCleaner.util.IdeaProjectUtil
import javax.swing.JComponent
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

abstract class AbstractIssueTab(
    private val ideaProject: Project,
    private val toolWindow: ToolWindow,
    private val componentFactory: ComponentFactory
) : TreeSelectionListener {
    protected abstract val dividerKey: String

    protected abstract val showUsagePreviewPanel: Boolean

    protected abstract fun getIssues(store: AppStore): Collection<Issue>

    protected abstract fun findIssue(store: AppStore, id: String): Issue?

    protected val store = componentFactory.makeDispatcher().store

    private val splitter by lazy {
        OnePixelSplitter(false, dividerKey, 0.5f)
    }
    protected open val issueTree = IssueTree(ideaProject)
    protected open val usagePreviewPanel = UsagePreviewPanel(
        ideaProject,
        FindInProjectUtil.setupViewPresentation(false, FindModel())
    )

    fun createPanel(): JComponent {
        store.onChange("project", this::updateComponents)

        issueTree.addTreeSelectionListener(this)

        return if (showUsagePreviewPanel) {
            splitter.firstComponent = ScrollPaneFactory.createScrollPane(issueTree.component)
            splitter.secondComponent = usagePreviewPanel
            splitter
        } else {
            ScrollPaneFactory.createScrollPane(issueTree.component)
        }
    }

    override fun valueChanged(e: TreeSelectionEvent?) {
        if (null === e) {
            return
        }

        val path = e.path
        when (val node = (path.lastPathComponent as DefaultMutableTreeNode).userObject) {
            is FileNode -> onFileNodeSelected(node)
            is MainIssueNode -> onMainIssueNodeSelected(path, node)
            is RelatedIssueNode -> onRelatedIssueNodeSelected(path, node)
        }
    }

    protected open fun onFileNodeSelected(node: FileNode) {
        val file = IdeaProjectUtil.findVirtualFileByPath(node.data.value[ISSUE_NODE_VALUE_PATH] as String)
        if (null !== file) {
            FileEditorManager.getInstance(ideaProject).openFile(file, false)
        }
    }

    protected open fun onMainIssueNodeSelected(path: TreePath, node: MainIssueNode) {
        val fileNode = (path.parentPath.lastPathComponent as DefaultMutableTreeNode).userObject as? FileNode ?: return
        val file = IdeaProjectUtil.findVirtualFileByPath(fileNode.data.value[ISSUE_NODE_VALUE_PATH] as String)
        val issue = findIssue(store, node.data.issueId)
        if (null !== file && null !== issue) {
            val descriptor = OpenFileDescriptor(ideaProject, file, issue.lines.begin - 1, 0, true)
            FileEditorManager.getInstance(ideaProject).openEditor(descriptor, false)
        }
    }

    protected open fun onRelatedIssueNodeSelected(path: TreePath, node: RelatedIssueNode) {
        val psiFile = IdeaProjectUtil.findPsiFile(ideaProject, node.data.value[ISSUE_NODE_VALUE_PATH] as String)
        val infos = mutableListOf<UsageInfo>()
        if (null !== psiFile) {
            val document = PsiDocumentManager.getInstance(ideaProject).getDocument(psiFile)
            if (null !== document) {
                val startOffset = document.getLineStartOffset((node.data.value[ISSUE_NODE_VALUE_LINE_BEGIN] as Int)-1)
                val endOffset = document.getLineEndOffset((node.data.value[ISSUE_NODE_VALUE_LINE_END] as Int)-1)
                infos.add(UsageInfo(psiFile, startOffset, endOffset))
            }
        }
        println(node.data)
        usagePreviewPanel.updateLayout(infos)
    }

    protected open fun updateComponents() {
        if (!store.project.hasResult) {
            issueTree.updateBy(listOf(), store.project.id, store.project.basePath)
        } else {
            issueTree.updateBy(getIssues(store), store.project.id, store.project.basePath)
        }
    }
}