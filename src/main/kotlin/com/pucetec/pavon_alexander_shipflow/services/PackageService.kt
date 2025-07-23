package com.pucetec.pavon_alexander_shipflow.services

import com.pucetec.pavon_alexander_shipflow.exceptions.exceptions.*
import com.pucetec.pavon_alexander_shipflow.mappers.PackageEventMapper
import com.pucetec.pavon_alexander_shipflow.models.entities.Package
import com.pucetec.pavon_alexander_shipflow.models.entities.PackageEvent
import com.pucetec.pavon_alexander_shipflow.models.entities.Status
import com.pucetec.pavon_alexander_shipflow.models.requests.CreatePackageRequest
import com.pucetec.pavon_alexander_shipflow.models.requests.UpdatePackageStatusRequest
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageEventHistoryResponse
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageSummaryResponse
import com.pucetec.pavon_alexander_shipflow.repositories.PackageEventRepository
import com.pucetec.pavon_alexander_shipflow.repositories.PackageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PackageService(
    private val packageRepository: PackageRepository,
    private val packageEventRepository: PackageEventRepository,
    private val packageEventMapper: PackageEventMapper
) {

    @Transactional
    fun create(request: CreatePackageRequest): Package {
        val origin = request.cityFrom.trim().uppercase()
        val destination = request.cityTo.trim().uppercase()

        if (origin == destination) {
            throw SameCityException("La ciudad de origen no puede ser igual a la ciudad de destino")
        }

        if (request.description.length > 50) {
            throw DescriptionTooLongException("La descripción no puede superar los 50 caracteres")
        }

        val packageEntity = Package(
            type = request.type,
            weight = request.weight,
            description = request.description.trim(),
            cityFrom = origin,
            cityTo = destination,
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5),
            events = listOf()
        )

        val savedPackage = packageRepository.save(packageEntity)

        val initialEvent = PackageEvent(
            status = Status.PENDING,
            comment = "Registro inicial",
            packageEntity = savedPackage
        )

        packageEventRepository.save(initialEvent)

        return savedPackage.copy(events = listOf(initialEvent))
    }


    fun getAll(): List<Package> {
        return packageRepository.findAll()
    }


    fun getByTrackingId(trackingId: String): Package {
        return packageRepository.findByTrackingId(trackingId)
            ?: throw ResourceNotFoundException("Paquete con trackingId $trackingId no encontrado")
    }


    @Transactional
    fun updateStatusByTrackingId(trackingId: String, request: UpdatePackageStatusRequest): PackageEvent {
        val packageEntity = packageRepository.findByTrackingId(trackingId)
            ?: throw ResourceNotFoundException("Package with trackingId $trackingId not found")

        val currentStatus = packageEntity.events.maxByOrNull { it.createdAt }?.status

        if (!isValidTransition(currentStatus, request.status)) {
            throw InvalidStatusTransitionException("Transición inválida de estado: $currentStatus → ${request.status}")
        }

        val event = packageEventMapper.toEntity(request, packageEntity)
        return packageEventRepository.save(event)
    }


    fun getPackageWithEventsByTrackingId(trackingId: String): PackageEventHistoryResponse {
        val packageEntity = getByTrackingId(trackingId)

        val summary = PackageSummaryResponse(
            id = packageEntity.id,
            trackingId = packageEntity.trackingId,
            type = packageEntity.type,
            cityFrom = packageEntity.cityFrom,
            cityTo = packageEntity.cityTo
        )

        val events = packageEntity.events
            .sortedByDescending { it.createdAt }
            .map { packageEventMapper.toResponse(it) }

        return PackageEventHistoryResponse(
            `package` = summary,
            events = events
        )
    }


    private fun isValidTransition(current: Status?, next: Status): Boolean {
        return when (current) {
            null -> next == Status.PENDING
            Status.PENDING -> next == Status.IN_TRANSIT
            Status.IN_TRANSIT -> next in listOf(Status.DELIVERED, Status.ON_HOLD, Status.CANCELLED)
            Status.ON_HOLD -> next in listOf(Status.IN_TRANSIT, Status.CANCELLED)
            else -> false
        }
    }
}
