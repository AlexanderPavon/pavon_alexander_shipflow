package com.pucetec.pavon_alexander_shipflow.models.responses

data class PackageEventHistoryResponse(
    val `package`: PackageSummaryResponse,
    val events: List<PackageEventResponse>
)
