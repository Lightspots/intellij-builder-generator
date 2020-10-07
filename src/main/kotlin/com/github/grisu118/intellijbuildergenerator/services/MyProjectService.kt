package com.github.grisu118.intellijbuildergenerator.services

import com.intellij.openapi.project.Project
import com.github.grisu118.intellijbuildergenerator.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
