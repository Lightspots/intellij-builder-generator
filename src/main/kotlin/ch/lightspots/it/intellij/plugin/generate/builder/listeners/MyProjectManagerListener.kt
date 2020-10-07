package ch.lightspots.it.intellij.plugin.generate.builder.listeners

import ch.lightspots.it.intellij.plugin.generate.builder.services.MyProjectService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.getService(MyProjectService::class.java)
    }
}
