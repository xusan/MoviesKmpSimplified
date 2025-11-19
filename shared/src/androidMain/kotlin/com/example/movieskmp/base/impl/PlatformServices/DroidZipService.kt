package com.base.impl.Droid.PlatformServices

import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.Platform.IZipService
import com.base.impl.Diagnostic.LoggableService
import java.io.*
import java.util.zip.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DroidZipService : LoggableService(), IZipService
{
    init
    {
        InitSpecificlogger(SpecificLoggingKeys.LogEssentialServices)
    }

    override suspend fun CreateFromDirectoryAsync(dirPath: String, zipPath: String)
    {
        SpecificLogMethodStart(::CreateFromDirectoryAsync.name, dirPath, zipPath)
       return withContext(Dispatchers.IO) {
            val dir = File(dirPath)
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipPath))).use { zipOut ->
                dir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val entryName = file.relativeTo(dir).path.replace(File.separatorChar, '/')
                        zipOut.putNextEntry(ZipEntry(entryName))
                        file.inputStream().copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }

    override suspend fun ExtractToDirectoryAsync(zipPath: String, dirPath: String, overwrite: Boolean)
    {
        SpecificLogMethodStart(::ExtractToDirectoryAsync.name,zipPath, dirPath, overwrite)

        return withContext(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            ZipInputStream(BufferedInputStream(FileInputStream(zipPath))).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val outFile = File(dirPath, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        if (overwrite || !outFile.exists()) {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { out ->
                                var len: Int
                                while (zipIn.read(buffer).also { len = it } > 0) {
                                    out.write(buffer, 0, len)
                                }
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        }
    }

    override suspend fun CreateFromFileAsync(filePath: String, zipPath: String)
    {
        SpecificLogMethodStart(::CreateFromFileAsync.name,filePath, zipPath)

        return withContext(Dispatchers.IO) {
            val file = File(filePath)
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipPath))).use { zipOut ->
                val entryName = file.name
                zipOut.putNextEntry(ZipEntry(entryName))
                file.inputStream().copyTo(zipOut)
                zipOut.closeEntry()
            }
        }
    }
}