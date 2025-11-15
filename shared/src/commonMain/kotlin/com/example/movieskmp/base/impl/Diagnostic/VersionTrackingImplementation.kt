package com.base.impl.Diagnostic

import com.base.abstractions.Essentials.IAppInfo
import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.Essentials.IVersionTracking
import org.koin.core.component.inject
import kotlin.getValue

internal class VersionTrackingImplementation: LoggableService(), IVersionTracking
{
    private val preferences: IPreferences by inject()
    private val appInfo: IAppInfo by inject()

    private val versionsKey = "VersionTracking.Versions"
    private val buildsKey = "VersionTracking.Builds"
    private val sharedName = "${appInfo.PackageName}.essentials.versiontracking"

    private lateinit var versionTrail: MutableMap<String, MutableList<String>>

    private val LastInstalledVersion: String
        get() = versionTrail[versionsKey]?.lastOrNull() ?: ""

    private val LastInstalledBuild: String
        get() = versionTrail[buildsKey]?.lastOrNull() ?: ""

    init
    {
        Track()
    }

    override fun Track()
    {
        LogMethodStart(::Track.name)
        if (::versionTrail.isInitialized)
            return

        InitVersionTracking()
    }

    /// Initialize VersionTracking module, load data and track current version
    /// For internal use. Usually only called once in production code, but multiple times in unit tests
    internal fun InitVersionTracking()
    {
        LogMethodStart(::InitVersionTracking.name)
        IsFirstLaunchEver = !preferences.ContainsKey(versionsKey, sharedName) || !preferences.ContainsKey(buildsKey, sharedName)
        if (IsFirstLaunchEver)
        {
            versionTrail = mutableMapOf(
                versionsKey to mutableListOf(),
                buildsKey to mutableListOf()
            )
        }
        else
        {
            versionTrail = mutableMapOf(
                versionsKey to ReadHistory(versionsKey).toMutableList(),
                buildsKey to ReadHistory(buildsKey).toMutableList()
            )
        }

        IsFirstLaunchForCurrentVersion = !versionTrail[versionsKey]!!.contains(CurrentVersion) || CurrentVersion != LastInstalledVersion
        if (IsFirstLaunchForCurrentVersion)
        {
            // Avoid duplicates and move current version to end of list if already present
            versionTrail[versionsKey]!!.removeAll { v -> v == CurrentVersion }
            versionTrail[versionsKey]!!.add(CurrentVersion)
        }

        IsFirstLaunchForCurrentBuild = !versionTrail[buildsKey]!!.contains(CurrentBuild) || CurrentBuild != LastInstalledBuild
        if (IsFirstLaunchForCurrentBuild)
        {
            // Avoid duplicates and move current build to end of list if already present
            versionTrail[buildsKey]!!.removeAll { b -> b == CurrentBuild }
            versionTrail[buildsKey]!!.add(CurrentBuild)
        }

        if (IsFirstLaunchForCurrentVersion || IsFirstLaunchForCurrentBuild)
        {
            WriteHistory(versionsKey, versionTrail[versionsKey]!!)
            WriteHistory(buildsKey, versionTrail[buildsKey]!!)
        }
    }

    override var IsFirstLaunchEver: Boolean = false
        private set

    override var IsFirstLaunchForCurrentVersion: Boolean = false
        private set

    override var IsFirstLaunchForCurrentBuild: Boolean = false
        private set

    override val CurrentVersion: String
        get() = appInfo.VersionString

    override val CurrentBuild: String
        get() = appInfo.BuildString

    override val PreviousVersion: String?
        get() = GetPrevious(versionsKey)

    override val PreviousBuild: String?
        get() = GetPrevious(buildsKey)

    override val FirstInstalledVersion: String?
        get() = versionTrail[versionsKey]?.firstOrNull()

    override val FirstInstalledBuild: String?
        get() = versionTrail[buildsKey]?.firstOrNull()

    override val VersionHistory: List<String>
        get() = versionTrail[versionsKey]?.toList() ?: emptyList()

    override val BuildHistory: List<String>
        get() = versionTrail[buildsKey]?.toList() ?: emptyList()

    override fun IsFirstLaunchForVersion(version: String): Boolean {
        LogMethodStart(::IsFirstLaunchForVersion.name, version)
        return CurrentVersion == version && IsFirstLaunchForCurrentVersion
    }

    override fun IsFirstLaunchForBuild(build: String): Boolean {
        LogMethodStart(::IsFirstLaunchForBuild.name, build)
        return CurrentBuild == build && IsFirstLaunchForCurrentBuild
    }

    fun GetStatus(): String
    {
        LogMethodStart(::GetStatus.name)
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("VersionTracking")
        sb.appendLine("  IsFirstLaunchEver:              $IsFirstLaunchEver")
        sb.appendLine("  IsFirstLaunchForCurrentVersion: $IsFirstLaunchForCurrentVersion")
        sb.appendLine("  IsFirstLaunchForCurrentBuild:   $IsFirstLaunchForCurrentBuild")
        sb.appendLine()
        sb.appendLine("  CurrentVersion:                 $CurrentVersion")
        sb.appendLine("  PreviousVersion:                $PreviousVersion")
        sb.appendLine("  FirstInstalledVersion:          $FirstInstalledVersion")
        sb.appendLine("  VersionHistory:                 [${VersionHistory.joinToString(", ")}]")
        sb.appendLine()
        sb.appendLine("  CurrentBuild:                   $CurrentBuild")
        sb.appendLine("  PreviousBuild:                  $PreviousBuild")
        sb.appendLine("  FirstInstalledBuild:            $FirstInstalledBuild")
        sb.appendLine("  BuildHistory:                   [${BuildHistory.joinToString(", ")}]")
        return sb.toString()
    }

    private fun ReadHistory(key: String): List<String> {
        LogMethodStart(::ReadHistory.name, key)
        return preferences.Get<String?>(key, null, sharedName)?.split('|')?.filter { it.isNotEmpty() } ?: emptyList()
    }

    private fun WriteHistory(key: String, history: List<String>) {
        LogMethodStart(::WriteHistory.name, key, history)
        preferences.Set(key, history.joinToString("|"), sharedName)
    }

    private fun GetPrevious(key: String): String?
    {
        LogMethodStart(::GetPrevious.name, key)
        val trail = versionTrail[key]!!
        return if (trail.size >= 2) trail[trail.size - 2] else null
    }
}