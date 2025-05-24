package versioning

import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

/**
 * BuildConfig provides access to user-defined build flags that control how a Grim
 * build is assembled. These flags are resolved once at configuration time using
 * [init], and are then exposed as fast, memoized values.
 *
 * Flags can be defined in three ways:
 *
 *   ① JVM system properties  (-Dflag=value)
 *   ② Gradle project properties  (-Pflag=value or in gradle.properties)
 *   ③ Environment variables  (FLAG=value)
 *
 * You can use these to enable/disable features like shading, relocation, or release mode.
 *
 * Examples:
 *
 * Using Gradle -P properties:
 * ```
 * ./gradlew build -PshadePE=true -Prelocate=false -Prelease=true
 * ```
 *
 * Using environment variables:
 * ```
 * SHADE_PE=true RELOCATE_JAR=false RELEASE=true ./gradlew build
 * ```
 *
 * Using JVM system properties:
 * ```
 * ./gradlew build -DshadePE=true -Drelease=true
 * ```
 *
 * @property shadePE  If true, shades PacketEvents into the jar. Default: true.
 * @property relocate If true, relocates shaded dependencies to avoid conflicts. Default: true.
 * @property release  If true, omits commit hash and modifiers from version string. Default: false.
 * @property mavenLocalOverride If true, will make artifacts in mavenLocal() will be used instead of their remote counterparts for this build. Default: false
 */
object BuildConfig {

    /**
     * Must be called once from your root build script to initialize the flags.
     * Example (in build.gradle.kts):
     * ```
     * BuildConfig.init(project)
     * ```
     */
    fun init(project: Project) {
        _shadePE = resolveBool(project, "shadePE", altKey = "SHADE_PE", default = true)
        _relocate = resolveBool(project, "relocate", altKey = "RELOCATE_JAR", default = true)
        _release = resolveBool(project, "release", default = false)
        _mavenLocalOverride = resolveBool(project, "mavenLocalOverride", default = false)
    }

    // Unified resolution logic (System > Gradle > Env)
    private fun resolveRaw(project: Project, key: String): String? =
        System.getProperty(key)                       // ① JVM   (-Dkey=value)
            ?: project.findProperty(key)?.toString()  // ② Gradle (-Pkey=value or gradle.properties)
            ?: System.getenv(key.uppercase())         // ③ ENV   (KEY=value)

    private fun resolveBool(project: Project, key: String, altKey: String? = null, default: Boolean): Boolean {
        val primaryValue = resolveRaw(project, key)?.toDefaultLowerCase()?.toBooleanStrictOrNull()
        if (primaryValue != null) {
            return primaryValue
        }

        if (altKey != null) {
            val altValue = resolveRaw(project, altKey)?.toDefaultLowerCase()?.toBooleanStrictOrNull()
            if (altValue != null) {
                return altValue
            }
        }

        return default
    }

    // Private backing vars (nullable because we can't use lateinit with primitives)
    private var _shadePE: Boolean? = null
    private var _relocate: Boolean? = null
    private var _release: Boolean? = null
    private var _mavenLocalOverride: Boolean? = null

    /** If true, shades PacketEvents into the jar. Default: true. */
    val shadePE: Boolean get() = _shadePE
        ?: error("BuildConfig.shadePE accessed before init() was called")

    /** If true, relocates shaded dependencies to avoid conflicts. Default: true. */
    val relocate: Boolean get() = _relocate
        ?: error("BuildConfig.relocate accessed before init() was called")

    /** If true, omits commit hash and modifiers from version string. Default: false. */
    val release: Boolean get() = _release
        ?: error("BuildConfig.release accessed before init() was called")

    val mavenLocalOverride: Boolean get() = _mavenLocalOverride
        ?: error("BuildConfig.release accessed before init() was called")
}