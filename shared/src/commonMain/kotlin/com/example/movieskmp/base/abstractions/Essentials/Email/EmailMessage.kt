package com.base.abstractions.Essentials.Email

/**
 * Represents a single email message.
 */
class EmailMessage
{
    /**
     * Initializes a new instance of the EmailMessage class.
     */
    constructor()

    /**
     * Initializes a new instance of the EmailMessage class with the specified subject, body, and recipients.
     * @param subject The email subject.
     * @param body The email body.
     * @param to The email recipients.
     */
    constructor(subject: String, body: String, vararg to: String)
    {
        Subject = subject
        Body = body
        To = to?.toMutableList() ?: mutableListOf()
    }

    /**
     * Gets or sets the email's subject.
     */
    var Subject: String? = null

    /**
     * Gets or sets the email's body.
     */
    var Body: String? = null

    /**
     * Gets or sets a value indicating whether the message is in plain text or HTML.
     * Remarks: EmailBodyFormat.Html is not supported on Windows.
     */
    var BodyFormat: EmailBodyFormat = EmailBodyFormat.PlainText

    /**
     * Gets or sets the email's recipients.
     */
    var To: MutableList<String> = mutableListOf()

    /**
     * Gets or sets the email's CC (Carbon Copy) recipients.
     */
    var Cc: MutableList<String> = mutableListOf()

    /**
     * Gets or sets the email's BCC (Blind Carbon Copy) recipients.
     */
    var Bcc: MutableList<String> = mutableListOf()

    /**
     * Gets or sets a list of file attachments as EmailAttachment objects.
     */
    var Attachments: MutableList<EmailAttachment> = mutableListOf()
}