package com.heartspell.vertxorbit.service

import com.heartspell.vertxorbit.lifecycle.LifecycleFinding
import com.heartspell.vertxorbit.navigation.SourceTarget

data class OrbitFindingEntry(
    val id: String,
    val line: String,
    val finding: LifecycleFinding,
    val target: SourceTarget
)

data class OrbitAnalysisResult(
    val fileName: String,
    val verticles: List<ActiveVerticleEntry>,
    val findings: List<OrbitFindingEntry>,
    val deployments: List<ActiveDeploymentEntry>
)

data class ActiveVerticleEntry(
    val id: String,
    val className: String,
    val line: String,
    val target: SourceTarget,
    val findings: List<OrbitFindingEntry>
) {
    val issues: Int = findings.count { it.finding.severity.name == "WARNING" }
    val managed: Int = findings.size - issues
}

data class ActiveDeploymentEntry(
    val id: String,
    val target: String,
    val targetSimpleName: String?,
    val line: String,
    val source: SourceTarget,
    val isDynamic: Boolean
)
