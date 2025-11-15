package com.base.abstractions.Common

class VersionInfo private constructor(
    val major: Int,
    val minor: Int,
    val build: Int = -1,
    val revision: Int = -1
) : Comparable<VersionInfo> {

    init {
        require(major >= 0) { "Major version cannot be negative." }
        require(minor >= 0) { "Minor version cannot be negative." }
        if (build != -1) require(build >= 0) { "Build cannot be negative." }
        if (revision != -1) require(revision >= 0) { "Revision cannot be negative." }
    }

    constructor(major: Int, minor: Int) : this(major, minor, -1, -1)
    constructor(major: Int, minor: Int, build: Int) : this(major, minor, build, -1)
    constructor(version: String) : this(
        parse(version).major,
        parse(version).minor,
        parse(version).build,
        parse(version).revision
    )

    //public override fun clone(): Version = Version(major, minor, build, revision)

    // region --- Computed properties ---
    val majorRevision: Short get() = (revision shr 16).toShort()
    val minorRevision: Short get() = (revision and 0xFFFF).toShort()
    // endregion

    // region --- Comparison ---
    override fun compareTo(other: VersionInfo): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (build != other.build) return build.compareTo(other.build)
        if (revision != other.revision) return revision.compareTo(other.revision)
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VersionInfo) return false
        return major == other.major &&
                minor == other.minor &&
                build == other.build &&
                revision == other.revision
    }

    override fun hashCode(): Int {
        var acc = 0
        acc = acc or ((major and 0x0000000F) shl 28)
        acc = acc or ((minor and 0x000000FF) shl 20)
        acc = acc or ((build and 0x000000FF) shl 12)
        acc = acc or (revision and 0x00000FFF)
        return acc
    }

    override fun toString(): String {
        val parts = mutableListOf(major, minor)
        if (build >= 0) parts.add(build)
        if (revision >= 0) parts.add(revision)
        return parts.joinToString(".")
    }
    // endregion

    // region --- Companion: parsing, tryParse ---
    companion object Companion
    {

        fun ParseVersion(version: String): VersionInfo
        {
            try
            {
                return parse(version)
            }
            catch (e: Exception)
            {
                // Continue to next parsing attempt
            }

            val major = version.toIntOrNull()
            if (major != null)
            {
                return VersionInfo(major, 0)
            }

            return VersionInfo(0, 0)
        }

        fun parse(input: String): VersionInfo {
            require(input.isNotBlank()) { "Input cannot be blank" }
            val parts = input.split('.')
            if (parts.size < 2 || parts.size > 4)
                throw IllegalArgumentException("Invalid version format: $input")

            fun parsePart(s: String, name: String): Int {
                val v = s.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid number in version part: $name = $s")
                require(v >= 0) { "Negative version component: $name" }
                return v
            }

            val major = parsePart(parts[0], "major")
            val minor = parsePart(parts[1], "minor")
            val build = if (parts.size >= 3) parsePart(parts[2], "build") else -1
            val revision = if (parts.size == 4) parsePart(parts[3], "revision") else -1
            return VersionInfo(major, minor, build, revision)
        }

        fun tryParse(input: String?): VersionInfo? = try {
            if (input == null) null else parse(input)
        } catch (_: Exception) {
            null
        }
    }
    // endregion

    // region --- Operators ---
    fun compareToNullable(other: VersionInfo?): Int =
        if (other == null) 1 else compareTo(other)

    operator fun component1() = major
    operator fun component2() = minor
    operator fun component3() = build
    operator fun component4() = revision

    fun equals(other: VersionInfo?) = this.compareToNullable(other) == 0

    operator fun not() = this
    // endregion
}

// region --- Operators for global use ---
operator fun VersionInfo?.compareTo(other: VersionInfo?): Int {
    if (this == null) return if (other == null) 0 else -1
    return this.compareToNullable(other)
}