package com.pucetec.pavon_alexander_shipflow.models.requests

import com.fasterxml.jackson.annotation.JsonProperty
import com.pucetec.pavon_alexander_shipflow.models.entities.Type

data class CreatePackageRequest(
    val type: Type,
    val weight: Float,
    val description: String,
    @JsonProperty("city_from")
    val cityFrom: String,
    @JsonProperty("city_to")
    val cityTo: String,
)
