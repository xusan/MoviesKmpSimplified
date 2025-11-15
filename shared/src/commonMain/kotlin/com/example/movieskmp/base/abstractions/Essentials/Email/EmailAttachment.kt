package com.base.abstractions.Essentials.Email

/**
 * Represents a email file attachment.
 */
class EmailAttachment
{
    var FullPath: String
    var ContentType: String
    /**
     * Initializes a new instance of the EmailAttachment class based off the file specified in the provided path
     * and providing an explicit MIME filetype.
     * @param fullPath Full path and filename to file on filesystem.
     * @param contentType Content type (MIME type) of the file (e.g.: image/png).
     */
    constructor(fullPath: String, contentType: String)
    {
        FullPath = fullPath
        ContentType = contentType
    }

}