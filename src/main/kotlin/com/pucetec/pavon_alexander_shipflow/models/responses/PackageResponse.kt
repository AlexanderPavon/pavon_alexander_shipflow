package com.pucetec.pavon_alexander_shipflow.models.responses

import com.fasterxml.jackson.annotation.JsonProperty
import com.pucetec.pavon_alexander_shipflow.models.entities.Status
import com.pucetec.pavon_alexander_shipflow.models.entities.Type
import java.time.LocalDateTime

data class PackageResponse(
    val trackingId: String,
    val type: Type,
    val weight: Float,
    val description: String,
    @JsonProperty("city_from")
    val cityFrom: String,
    @JsonProperty("city_to")
    val cityTo: String,
    val createdAt: LocalDateTime,
    val estimatedDeliveryDate: LocalDateTime,
    val currentStatus: Status
)