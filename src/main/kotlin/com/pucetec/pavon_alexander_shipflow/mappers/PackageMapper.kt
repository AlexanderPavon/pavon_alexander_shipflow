package com.pucetec.pavon_alexander_shipflow.mappers

import com.pucetec.pavon_alexander_shipflow.models.entities.Package
import com.pucetec.pavon_alexander_shipflow.models.entities.PackageEvent
import com.pucetec.pavon_alexander_shipflow.models.entities.Status
import com.pucetec.pavon_alexander_shipflow.models.requests.CreatePackageRequest
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageResponse
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class PackageMapper {

    fun toEntity(request: CreatePackageRequest, generateTrackingId: Boolean = false): Package {

        return Package(
            type = request.type,
            weight = request.weight,
            description = request.description,
            cityFrom = request.cityFrom,
            cityTo = request.cityTo,
            trackingId = if (generateTrackingId) UUID.randomUUID().toString() else "",
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5),
            events = listOf()
        )
    }

    fun toResponse(entity: Package): PackageResponse {
        val latestEvent = entity.events.maxByOrNull { it.createdAt }

        return PackageResponse(
            trackingId = entity.trackingId,
            type = entity.type,
            weight = entity.weight,
            description = entity.description,
            cityFrom = entity.cityFrom,
            cityTo = entity.cityTo,
            createdAt = entity.createdAt,
            estimatedDeliveryDate = entity.estimatedDeliveryDate,
            currentStatus = latestEvent?.status ?: Status.PENDING
        )
    }
}
