package com.base.abstractions.Essentials.Email

/**
 * Represents various types of email body formats.
 */
enum class EmailBodyFormat
{
    /**
     * The email message body is plain text.
     */
    PlainText,

    /**
     * The email message body is HTML (not supported on Windows).
     */
    Html
}