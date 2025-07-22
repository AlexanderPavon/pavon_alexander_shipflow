package com.pucetec.pavon_alexander_shipflow.models.responses

import com.pucetec.pavon_alexander_shipflow.models.entities.Type

data class PackageSummaryResponse(
    val id: Long,
    val trackingId: String,
    val type: Type,
    val cityFrom: String,
    val cityTo: String
)
