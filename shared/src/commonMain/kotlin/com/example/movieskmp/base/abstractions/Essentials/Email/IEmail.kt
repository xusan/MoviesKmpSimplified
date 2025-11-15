package com.base.abstractions.Essentials.Email

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Provides an easy way to allow the user to send emails.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IEmail", exact = true)
interface IEmail
{
    /**
     * Gets a value indicating whether composing an email is supported on this device.
     */
    val IsComposeSupported: Boolean

    /**
     * Opens the default email client to allow the user to send the message.
     * @param message Instance of EmailMessage containing details of the email message to compose.
     * @return A suspend function representing the asynchronous operation.
     */
    fun Compose(message: EmailMessage)
}