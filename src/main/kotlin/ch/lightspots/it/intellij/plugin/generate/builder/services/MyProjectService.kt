package ch.lightspots.it.intellij.plugin.generate.builder.services

import ch.lightspots.it.intellij.plugin.generate.builder.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
