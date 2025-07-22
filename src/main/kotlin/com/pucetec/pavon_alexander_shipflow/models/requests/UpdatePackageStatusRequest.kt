package com.pucetec.pavon_alexander_shipflow.models.requests

import com.pucetec.pavon_alexander_shipflow.models.entities.Status

data class UpdatePackageStatusRequest(
    val status: Status,
    val comment: String? = null
)
