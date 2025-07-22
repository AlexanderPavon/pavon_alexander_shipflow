package com.pucetec.pavon_alexander_shipflow.mappers

import com.pucetec.pavon_alexander_shipflow.models.entities.Package
import com.pucetec.pavon_alexander_shipflow.models.entities.PackageEvent
import com.pucetec.pavon_alexander_shipflow.models.requests.UpdatePackageStatusRequest
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageEventResponse
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageSummaryResponse
import org.springframework.stereotype.Component

@Component
class PackageEventMapper {

    fun toEntity(request: UpdatePackageStatusRequest, packageEntity: Package): PackageEvent {
        return PackageEvent(
            status = request.status,
            comment = request.comment,
            packageEntity = packageEntity
        )
    }

    fun toResponse(event: PackageEvent): PackageEventResponse {
        return PackageEventResponse(
            id = event.id,
            status = event.status,
            comment = event.comment,
            createdAt = event.createdAt,
            `package` = PackageSummaryResponse(
                id = event.packageEntity.id,
                trackingId = event.packageEntity.trackingId,
                type = event.packageEntity.type,
                cityFrom = event.packageEntity.cityFrom,
                cityTo = event.packageEntity.cityTo
            )
        )
    }
}