package com.example.movieskmp.domain.Infasructures

import com.base.abstractions.Diagnostic.ILoggingService
import com.base.impl.ContainerLocator
import io.sentry.Attachment
import io.sentry.kotlin.multiplatform.PlatformOptionsConfiguration
import kotlinx.coroutines.runBlocking

actual fun platformOptionsConfiguration(): PlatformOptionsConfiguration
{
    return { options->

        options.dsn = "https://c98fba15661f4aa06ab957467f5d9c4c@o4507288977080320.ingest.de.sentry.io/4510244083335248"
        // When first trying Sentry it's good to see what the SDK is doing:
        options.isDebug = false
        options.beforeSend = io.sentry.SentryOptions.BeforeSendCallback()
        { event, hint ->

            try
            {
                if (hint.attachments.isEmpty())
                {
                    val logger = ContainerLocator.Companion.Container?.get<ILoggingService>()
                    logger?.let()
                    {
                        // Log the error in app logs
                        it.LogError(event.throwable!!, "", handled = false)
                        val logBytes = runBlocking()
                        {
                            it.GetLastSessionLogBytes()
                        }
                        logBytes?.let()
                        {
                            // Add the file as attachment
                            hint.addAttachment(Attachment(logBytes, "appLog_${System.currentTimeMillis()}.zip", "application/x-zip-compressed"));
                        }
                    }
                }
            }
            catch (ex: Throwable)
            {
                ex.printStackTrace()
            }

            return@BeforeSendCallback event
        }
    }

}