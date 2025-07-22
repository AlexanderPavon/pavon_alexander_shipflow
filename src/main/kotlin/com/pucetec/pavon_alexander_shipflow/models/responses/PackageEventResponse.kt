package com.pucetec.pavon_alexander_shipflow.models.responses

import com.pucetec.pavon_alexander_shipflow.models.entities.Status
import java.time.LocalDateTime

data class PackageEventResponse(
    val id: Long,
    val status: Status,
    val comment: String?,
    val createdAt: LocalDateTime,
    val `package`: PackageSummaryResponse,
)
