package com.example.movieskmp.domain.Infasructures

import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Event
import io.sentry.kotlin.multiplatform.Attachment
import io.sentry.kotlin.multiplatform.PlatformOptionsConfiguration
import io.sentry.kotlin.multiplatform.Sentry

expect fun platformOptionsConfiguration(): PlatformOptionsConfiguration

class SentryErrorTracking : IErrorTrackingService
{
    override val OnServiceError = Event<Throwable>()

    override fun Initialize()
    {
        Sentry.initWithPlatformOptions(platformOptionsConfiguration())
    }

    override fun TrackError(ex: Throwable, attachment: ByteArray?, additionalData: Map<String, String>?)
    {
        try
        {
            if (attachment != null)
            {
                Sentry.captureException(ex, { scope ->

                    scope.addAttachment(Attachment(attachment, "appLog.zip", "application/x-zip-compressed"))
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