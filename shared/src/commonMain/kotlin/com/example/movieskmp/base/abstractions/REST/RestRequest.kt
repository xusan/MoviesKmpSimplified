package com.base.abstractions.REST

data class RestRequest(
    val ApiEndpoint: String,
    val RequestPriority: Priority = Priority.HIGH,
    val RequestTimeOut: TimeoutType = TimeoutType.Small,
    val CancelSameRequest: Boolean = false,
    val WithBearer: Boolean = true,
    val RequestBody: String? = null,
    val RetryCount: Int = 0,
    val HeaderValues: Map<String, String>? = null)