package com.example.movieskmp.Impl

import android.app.Application
import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Event
import com.base.impl.ContainerLocator
import io.sentry.Attachment
import io.sentry.Hint
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.runBlocking


class SentryErrorTracking(val application: Application) : IErrorTrackingService
{
    override val OnServiceError = Event<Throwable>()

    override fun Initialize()
    {
        SentryAndroid.init(application, { options ->
            options.dsn = "https://c98fba15661f4aa06ab957467f5d9c4c@o4507288977080320.ingest.de.sentry.io/4510244083335248"
            // When first trying Sentry it's good to see what the SDK is doing:
            options.isDebug = false

            options.beforeSend = object : SentryOptions.BeforeSendCallback {

                override fun execute(event: SentryEvent, hint: Hint): SentryEvent?
                {
                    try
                    {
                        if(hint.attachments.isEmpty())
                        {
                            val logger = ContainerLocator.Container?.get<ILoggingService>()
                            logger?.let()
                            {
                                // Log the error in app logs
                                it.LogError(event.throwable!!, "", handled = false)
                                val logBytes = runBlocking()
                                {
                                    it.GetLastSessionLogBytes()
                                }
                                logBytes?.let {
                                    hint.addAttachment(Attachment(it, "appLog_${System.currentTimeMillis()}.zip", "application/x-zip-compressed"));
                                }
                            }
                        }
                    }
                    catch (ex: Throwable)
                    {
                        ex.printStackTrace()
                    }

                    return event
                }
            }
        })

    }

    override fun TrackError(ex: Throwable, attachment: ByteArray?, additionalData: Map<String, String>?)
    {
        try
        {
            if (attachment != null)
            {
                Sentry.captureException(ex, { scope ->

                    scope.addAttachment(Attachment(attachment, "appLog_${System.currentTimeMillis()}.zip", "application/x-zip-compressed"))
                });
            }
            else
            {
                Sentry.captureException(ex);
            }
        }
        catch (error: Throwable)
        {
            OnServiceError.Invoke(error);
        }
    }
}