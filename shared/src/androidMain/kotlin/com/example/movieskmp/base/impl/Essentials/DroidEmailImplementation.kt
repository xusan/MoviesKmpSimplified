package com.base.impl.Droid.Essentials


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.text.Html
import android.text.Spanned
import com.base.abstractions.Essentials.Email.*
import com.base.impl.Droid.Essentials.Utils.*
import com.base.impl.Droid.Utils.CurrentActivity

internal class DroidEmailImplementation : IEmail
{

    override val IsComposeSupported: Boolean
        get() = PlatformUtils.IsIntentSupported(CreateIntent(testEmail))

    override fun Compose(message: EmailMessage)
    {
        if (!IsComposeSupported)
            throw UnsupportedOperationException("Email composing is not supported in this device.")

        val intent = CreateIntent(message)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        CurrentActivity.Instance.startActivity(intent)
    }


    val testEmail: EmailMessage
        get() = EmailMessage("Testing Essentials", "This is a test email.", "essentials@example.org")

    fun CreateIntent(message: EmailMessage): Intent
    {
        val action = when (message?.Attachments?.size ?: 0)
        {
            0 -> Intent.ACTION_SENDTO
            1 -> Intent.ACTION_SEND
            else -> Intent.ACTION_SEND_MULTIPLE
        }
        val intent = Intent(action)

        if (action == Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
        else
            intent.type = "message/rfc822"

        if (!message?.Body.isNullOrEmpty())
        {
            if (message!!.BodyFormat == EmailBodyFormat.Html)
            {
                val html: Spanned
                if (Build.VERSION.SDK_INT >= 24)
                {
                    html = Html.fromHtml(message.Body, Html.FROM_HTML_MODE_LEGACY)
                }
                else
                {
                    html = Html.fromHtml(message.Body)
                }
                intent.putExtra(Intent.EXTRA_TEXT, html)
            }
            else
            {
                intent.putExtra(Intent.EXTRA_TEXT, message.Body)
            }
        }
        if (!message?.Subject.isNullOrEmpty())
            intent.putExtra(Intent.EXTRA_SUBJECT, message.Subject)
        if ((message?.To?.size ?: 0) > 0)
            intent.putExtra(Intent.EXTRA_EMAIL, message.To.toTypedArray())
        if ((message?.Cc?.size ?: 0) > 0)
            intent.putExtra(Intent.EXTRA_CC, message.Cc.toTypedArray())
        if ((message?.Bcc?.size ?: 0) > 0)
            intent.putExtra(Intent.EXTRA_BCC, message.Bcc.toTypedArray())

        if ((message?.Attachments?.size ?: 0) > 0)
        {
            val uris = ArrayList<Parcelable>()
            for (attachment in message!!.Attachments)
            {

                uris.add(FileSystemUtils.GetShareableFileUri(attachment.FullPath))
            }

            if (uris.size > 1)
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            else
                intent.putExtra(Intent.EXTRA_STREAM, uris[0])

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return intent
    }

}


