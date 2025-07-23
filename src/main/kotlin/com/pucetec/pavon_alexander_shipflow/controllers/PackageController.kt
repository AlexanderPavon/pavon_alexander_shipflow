package com.pucetec.pavon_alexander_shipflow.controllers

import com.pucetec.pavon_alexander_shipflow.mappers.PackageEventMapper
import com.pucetec.pavon_alexander_shipflow.mappers.PackageMapper
import com.pucetec.pavon_alexander_shipflow.models.requests.CreatePackageRequest
import com.pucetec.pavon_alexander_shipflow.models.requests.UpdatePackageStatusRequest
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageEventHistoryResponse
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageEventResponse
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageResponse
import com.pucetec.pavon_alexander_shipflow.routes.Routes
import com.pucetec.pavon_alexander_shipflow.services.PackageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.PACKAGES)
class PackageController(
    private val packageService: PackageService,
    private val packageMapper: PackageMapper,
    private val packageEventMapper: PackageEventMapper
) {

    @PostMapping
    fun createPackage(@RequestBody request: CreatePackageRequest): ResponseEntity<PackageResponse> {
        val saved = packageService.create(request)
        return ResponseEntity.ok(packageMapper.toResponse(saved))
    }

    @GetMapping
    fun getAllPackages(): ResponseEntity<List<PackageResponse>> {
        val all = packageService.getAll().map { packageMapper.toResponse(it) }
        return ResponseEntity.ok(all)
    }

    @GetMapping("/{trackingId}")
    fun getPackageByTrackingId(@PathVariable trackingId: String): ResponseEntity<PackageResponse> {
        val pkg = packageService.getByTrackingId(trackingId)
        return ResponseEntity.ok(packageMapper.toResponse(pkg))
    }

    @PutMapping("/{trackingId}/status")
    fun updatePackageStatusByTrackingId(
        @PathVariable trackingId: String,
        @RequestBody request: UpdatePackageStatusRequest
    ): ResponseEntity<PackageEventResponse> {
        val event = packageService.updateStatusByTrackingId(trackingId, request)
        return ResponseEntity.ok(packageEventMapper.toResponse(event))
    }

@GetMapping("/{trackingId}/events")
fun getEventHistoryByTrackingId(@PathVariable trackingId: String): ResponseEntity<PackageEventHistoryResponse> {
    val response = packageService.getPackageWithEventsByTrackingId(trackingId)
    return ResponseEntity.ok(response)
}

    companion object {
        private const val ID = "id"
    }
}
