package com.pucetec.pavon_alexander_shipflow.services

import com.pucetec.pavon_alexander_shipflow.exceptions.exceptions.*
import com.pucetec.pavon_alexander_shipflow.mappers.PackageEventMapper
import com.pucetec.pavon_alexander_shipflow.models.entities.Package
import com.pucetec.pavon_alexander_shipflow.models.entities.PackageEvent
import com.pucetec.pavon_alexander_shipflow.models.entities.Status
import com.pucetec.pavon_alexander_shipflow.models.entities.Type
import com.pucetec.pavon_alexander_shipflow.models.requests.CreatePackageRequest
import com.pucetec.pavon_alexander_shipflow.models.requests.UpdatePackageStatusRequest
import com.pucetec.pavon_alexander_shipflow.repositories.PackageEventRepository
import com.pucetec.pavon_alexander_shipflow.repositories.PackageRepository
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageEventResponse
import com.pucetec.pavon_alexander_shipflow.models.responses.PackageSummaryResponse
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDateTime

class PackageServiceTest {

    private lateinit var packageRepository: PackageRepository
    private lateinit var packageEventRepository: PackageEventRepository
    private lateinit var packageEventMapper: PackageEventMapper
    private lateinit var packageService: PackageService

    @BeforeEach
    fun setUp() {
        packageRepository = mock(PackageRepository::class.java)
        packageEventRepository = mock(PackageEventRepository::class.java)
        packageEventMapper = mock(PackageEventMapper::class.java)
        packageService = PackageService(packageRepository, packageEventRepository, packageEventMapper)
    }

    @Test
    fun should_create_new_package_successfully() {
        val request = CreatePackageRequest(
            type = Type.SMALL_BOX,
            weight = 1.5f,
            description = "Audífonos",
            cityFrom = "Quito",
            cityTo = "Guayaquil"
        )

        val savedPackage = Package(
            type = Type.SMALL_BOX,
            weight = 1.5f,
            description = "Audífonos",
            cityFrom = "QUITO",
            cityTo = "GUAYAQUIL",
            estimatedDeliveryDate = LocalDateTime.now().plusDays(5)
        )

        val event = PackageEvent(
            status = Status.PENDING,
            comment = "Registro inicial",
            packageEntity = savedPackage
        )

        `when`(packageRepository.save(any(Package::class.java))).thenReturn(savedPackage)
        `when`(packageEventRepository.save(any(PackageEvent::class.java))).thenReturn(event)

        val result = packageService.create(request)

        assertEquals("QUITO", result.cityFrom)
        assertEquals("GUAYAQUIL", result.cityTo)
        assertEquals("Audífonos", result.description)
        assertEquals(Type.SMALL_BOX, result.type)
        assertEquals(Status.PENDING, result.events.first().status)
    }

    @Test
    fun should_throw_exception_when_origin_and_destination_are_the_same() {
        val request = CreatePackageRequest(
            type = Type.DOCUMENT,
            weight = 2.0f,
            description = "Contrato",
            cityFrom = "Quito",
            cityTo = "Quito"
        )

        assertThrows<SameCityException> {
            packageService.create(request)
        }
    }

    @Test
    fun should_throw_exception_when_description_is_too_long() {
        val request = CreatePackageRequest(
            type = Type.DOCUMENT,
            weight = 2.0f,
            description = "a".repeat(51),
            cityFrom = "Quito",
            cityTo = "Guayaquil"
        )

        assertThrows<DescriptionTooLongException> {
            packageService.create(request)
        }
    }

    @Test
    fun should_return_all_packages() {
        val pkg = Package(
            type = Type.DOCUMENT,
            weight = 0.5f,
            description = "Contrato",
            cityFrom = "Quito",
            cityTo = "Cuenca"
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(pkg))

        val result = packageService.getAll()

        assertEquals(1, result.size)
        assertEquals(Type.DOCUMENT, result[0].type)
    }

    @Test
    fun should_return_package_by_tracking_ID() {
        val pkg = Package(
            type = Type.FRAGILE,
            weight = 3.0f,
            description = "Lentes",
            cityFrom = "Quito",
            cityTo = "Cuenca"
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkg)

        val result = packageService.getByTrackingId("ABC123")

        assertEquals(Type.FRAGILE, result.type)
        assertEquals("Lentes", result.description)
    }

    @Test
    fun should_throw_exception_when_package_by_tracking_ID_not_found() {
        `when`(packageRepository.findByTrackingId("XYZ999")).thenReturn(null)

        assertThrows<ResourceNotFoundException> {
            packageService.getByTrackingId("XYZ999")
        }
    }

    @Test
    fun should_throw_exception_on_invalid_status_transition() {
        val pkg = Package(
            type = Type.SMALL_BOX,
            weight = 2.0f,
            description = "Audífonos",
            cityFrom = "Quito",
            cityTo = "Latacunga"
        )

        val event = PackageEvent(
            status = Status.DELIVERED,
            comment = "Finalizado",
            packageEntity = pkg
        )

        val pkgWithEvents = pkg.copy(events = listOf(event))

        val request = UpdatePackageStatusRequest(
            status = Status.IN_TRANSIT,
            comment = "Intento de reversa"
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkgWithEvents)

        assertThrows<InvalidStatusTransitionException> {
            packageService.updateStatusByTrackingId("ABC123", request)
        }
    }

    @Test
    fun should_return_package_with_events_by_tracking_ID() {
        val now = LocalDateTime.now()

        val pkg = Package(
            type = Type.FRAGILE,
            weight = 2.5f,
            description = "Lentes",
            cityFrom = "Loja",
            cityTo = "Ambato"
        )

        val mockEvent = mock(PackageEvent::class.java)
        `when`(mockEvent.status).thenReturn(Status.IN_TRANSIT)
        `when`(mockEvent.comment).thenReturn("En camino")
        `when`(mockEvent.createdAt).thenReturn(now)

        val pkgWithEvents = pkg.copy(events = listOf(mockEvent))

        val summary = PackageSummaryResponse(
            id = 99L,
            trackingId = pkgWithEvents.trackingId,
            type = Type.FRAGILE,
            cityFrom = "Loja",
            cityTo = "Ambato"
        )

        val expectedEventResponse = PackageEventResponse(
            id = 1L,
            status = Status.IN_TRANSIT,
            comment = "En camino",
            createdAt = now,
            `package` = summary
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkgWithEvents)
        `when`(packageEventMapper.toResponse(mockEvent)).thenReturn(expectedEventResponse)

        val result = packageService.getPackageWithEventsByTrackingId("ABC123")

        assertEquals(summary.trackingId, result.`package`.trackingId)
        assertEquals("Loja", result.`package`.cityFrom)
        assertEquals("Ambato", result.`package`.cityTo)
        assertEquals(1, result.events.size)
        assertEquals(Status.IN_TRANSIT, result.events[0].status)
        assertEquals("En camino", result.events[0].comment)
        assertEquals(now, result.events[0].createdAt)
    }

    @Test
    fun should_update_status_when_transition_is_valid() {
        val now = LocalDateTime.now()

        val pkg = Package(
            type = Type.SMALL_BOX,
            weight = 1.0f,
            description = "Audífonos",
            cityFrom = "Quito",
            cityTo = "Riobamba"
        )

        val event = mock(PackageEvent::class.java)
        `when`(event.createdAt).thenReturn(now.minusHours(1))
        `when`(event.status).thenReturn(Status.PENDING)

        val pkgWithEvents = pkg.copy(events = listOf(event))

        val request = UpdatePackageStatusRequest(
            status = Status.IN_TRANSIT,
            comment = "Salió de la bodega"
        )

        val newEvent = PackageEvent(
            status = Status.IN_TRANSIT,
            comment = "Salió de la bodega",
            packageEntity = pkgWithEvents
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkgWithEvents)
        `when`(packageEventMapper.toEntity(request, pkgWithEvents)).thenReturn(newEvent)
        `when`(packageEventRepository.save(newEvent)).thenReturn(newEvent)

        val result = packageService.updateStatusByTrackingId("ABC123", request)

        assertEquals(Status.IN_TRANSIT, result.status)
        assertEquals("Salió de la bodega", result.comment)
    }

    @Test
    fun should_allow_transition_from_null_to_PENDING() {
        val request = UpdatePackageStatusRequest(
            status = Status.PENDING,
            comment = "Creado sin eventos previos"
        )

        val pkg = Package(
            type = Type.DOCUMENT,
            weight = 1.0f,
            description = "Contrato",
            cityFrom = "Quito",
            cityTo = "Ibarra"
        )

        val newEvent = PackageEvent(
            status = Status.PENDING,
            comment = "Creado sin eventos previos",
            packageEntity = pkg
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkg)
        `when`(packageEventMapper.toEntity(request, pkg)).thenReturn(newEvent)
        `when`(packageEventRepository.save(newEvent)).thenReturn(newEvent)

        val result = packageService.updateStatusByTrackingId("ABC123", request)

        assertEquals(Status.PENDING, result.status)
    }

    @Test
    fun should_allow_transition_from_IN_TRANSIT_to_DELIVERED() {
        val now = LocalDateTime.now()
        val pkg = Package(
            type = Type.SMALL_BOX,
            weight = 2.0f,
            description = "Audífonos",
            cityFrom = "Quito",
            cityTo = "Loja"
        )

        val previousEvent = mock(PackageEvent::class.java)
        `when`(previousEvent.createdAt).thenReturn(now.minusHours(1))
        `when`(previousEvent.status).thenReturn(Status.IN_TRANSIT)

        val pkgWithEvents = pkg.copy(events = listOf(previousEvent))

        val request = UpdatePackageStatusRequest(
            status = Status.DELIVERED,
            comment = "Entregado al cliente"
        )

        val newEvent = PackageEvent(
            status = Status.DELIVERED,
            comment = "Entregado al cliente",
            packageEntity = pkgWithEvents
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkgWithEvents)
        `when`(packageEventMapper.toEntity(request, pkgWithEvents)).thenReturn(newEvent)
        `when`(packageEventRepository.save(newEvent)).thenReturn(newEvent)

        val result = packageService.updateStatusByTrackingId("ABC123", request)

        assertEquals(Status.DELIVERED, result.status)
    }

    @Test
    fun should_throw_exception_when_updating_status_for_nonexistent_package() {
        val request = UpdatePackageStatusRequest(
            status = Status.IN_TRANSIT,
            comment = "Intento con tracking incorrecto"
        )

        `when`(packageRepository.findByTrackingId("NOT_FOUND")).thenReturn(null)

        val exception = assertThrows<ResourceNotFoundException> {
            packageService.updateStatusByTrackingId("NOT_FOUND", request)
        }

        assertEquals("Package with trackingId NOT_FOUND not found", exception.message)
    }

    @Test
    fun should_allow_transition_from_ON_HOLD_to_CANCELLED() {
        val previousEvent = PackageEvent(
            status = Status.ON_HOLD,
            comment = "Incidente logístico",
            packageEntity = mock(Package::class.java)
        )

        val pkg = Package(
            type = Type.FRAGILE,
            weight = 1.0f,
            description = "Lentes",
            cityFrom = "Quito",
            cityTo = "Cuenca",
            events = listOf(previousEvent)
        )

        val request = UpdatePackageStatusRequest(
            status = Status.CANCELLED,
            comment = "Envío cancelado"
        )

        val newEvent = PackageEvent(
            status = Status.CANCELLED,
            comment = "Envío cancelado",
            packageEntity = pkg
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkg)
        `when`(packageEventMapper.toEntity(request, pkg)).thenReturn(newEvent)
        `when`(packageEventRepository.save(newEvent)).thenReturn(newEvent)

        val result = packageService.updateStatusByTrackingId("ABC123", request)

        assertEquals(Status.CANCELLED, result.status)
    }

    @Test
    fun should_reject_transition_from_null_to_CANCELLED() {
        val request = UpdatePackageStatusRequest(
            status = Status.CANCELLED,
            comment = "No válido desde null"
        )

        val pkg = Package(
            type = Type.DOCUMENT,
            weight = 1.0f,
            description = "Contrato",
            cityFrom = "Loja",
            cityTo = "Quito"
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkg)

        assertThrows<InvalidStatusTransitionException> {
            packageService.updateStatusByTrackingId("ABC123", request)
        }
    }

    @Test
    fun should_reject_transition_from_PENDING_to_DELIVERED() {
        val previousEvent = PackageEvent(
            status = Status.PENDING,
            comment = "Registro inicial",
            packageEntity = mock(Package::class.java)
        )

        val pkg = Package(
            type = Type.SMALL_BOX,
            weight = 2.0f,
            description = "Audífonos",
            cityFrom = "Quito",
            cityTo = "Ambato",
            events = listOf(previousEvent)
        )

        val request = UpdatePackageStatusRequest(
            status = Status.DELIVERED,
            comment = "Entrega directa no permitida"
        )

        `when`(packageRepository.findByTrackingId("ABC123")).thenReturn(pkg)

        assertThrows<InvalidStatusTransitionException> {
            packageService.updateStatusByTrackingId("ABC123", request)
        }
    }
}
