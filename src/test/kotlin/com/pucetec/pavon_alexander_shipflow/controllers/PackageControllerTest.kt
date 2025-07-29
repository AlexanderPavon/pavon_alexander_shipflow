package com.pucetec.pavon_alexander_shipflow.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.pavon_alexander_shipflow.exceptions.exceptions.ResourceNotFoundException
import com.pucetec.pavon_alexander_shipflow.mappers.PackageEventMapper
import com.pucetec.pavon_alexander_shipflow.mappers.PackageMapper
import com.pucetec.pavon_alexander_shipflow.models.entities.Status
import com.pucetec.pavon_alexander_shipflow.models.entities.Type
import com.pucetec.pavon_alexander_shipflow.models.requests.CreatePackageRequest
import com.pucetec.pavon_alexander_shipflow.models.requests.UpdatePackageStatusRequest
import com.pucetec.pavon_alexander_shipflow.models.responses.*
import com.pucetec.pavon_alexander_shipflow.routes.Routes
import com.pucetec.pavon_alexander_shipflow.services.PackageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import java.time.LocalDateTime
import kotlin.test.assertEquals

@WebMvcTest(PackageController::class)
class PackageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var packageService: PackageService

    @Autowired
    private lateinit var packageMapper: PackageMapper

    @Autowired
    private lateinit var packageEventMapper: PackageEventMapper

    private lateinit var objectMapper: ObjectMapper
    private val baseUrl = Routes.PACKAGES

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Test
    fun should_create_package_when_post() {
        val request = CreatePackageRequest(Type.SMALL_BOX, 2.5f, "Audífonos", "Quito", "Guayaquil")
        val pkg = mock(com.pucetec.pavon_alexander_shipflow.models.entities.Package::class.java)
        val response = PackageResponse(
            trackingId = "ABC123",
            type = Type.SMALL_BOX,
            weight = 2.5f,
            description = "Audífonos",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            createdAt = LocalDateTime.now(),
            estimatedDeliveryDate = LocalDateTime.now().plusDays(3),
            currentStatus = Status.PENDING
        )

        `when`(packageService.create(request)).thenReturn(pkg)
        `when`(packageMapper.toResponse(pkg)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.trackingId") { value("ABC123") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_get_all_packages_when_get() {
        val responseList = listOf(
            PackageResponse(
                "ABC123",
                Type.DOCUMENT,
                1.0f,
                "Contrato",
                "Quito",
                "Loja",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Status.PENDING
            )
        )
        val entityList = listOf(mock(com.pucetec.pavon_alexander_shipflow.models.entities.Package::class.java))

        `when`(packageService.getAll()).thenReturn(entityList)
        `when`(packageMapper.toResponse(entityList[0])).thenReturn(responseList[0])

        val result = mockMvc.get(baseUrl)
            .andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(1) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_get_package_by_tracking_id() {
        val trackingId = "ABC123"
        val entity = mock(com.pucetec.pavon_alexander_shipflow.models.entities.Package::class.java)
        val response = PackageResponse(
            trackingId,
            Type.DOCUMENT,
            1.0f,
            "Contrato",
            "Loja",
            "Ibarra",
            LocalDateTime.now(),
            LocalDateTime.now(),
            Status.PENDING
        )

        `when`(packageService.getByTrackingId(trackingId)).thenReturn(entity)
        `when`(packageMapper.toResponse(entity)).thenReturn(response)

        val result = mockMvc.get("$baseUrl/$trackingId")
            .andExpect {
                status { isOk() }
                jsonPath("$.trackingId") { value("ABC123") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_404_when_tracking_id_not_found() {
        val trackingId = "NOTFOUND"
        `when`(packageService.getByTrackingId(trackingId)).thenThrow(ResourceNotFoundException("Package not found"))

        val result = mockMvc.get("$baseUrl/$trackingId")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_update_status_when_put() {
        val trackingId = "ABC123"
        val request = UpdatePackageStatusRequest(Status.IN_TRANSIT, "Despachado")
        val eventEntity = mock(com.pucetec.pavon_alexander_shipflow.models.entities.PackageEvent::class.java)
        val eventResponse = PackageEventResponse(
            id = 1L,
            status = Status.IN_TRANSIT,
            comment = "Despachado",
            createdAt = LocalDateTime.now(),
            `package` = PackageSummaryResponse(1L, trackingId, Type.SMALL_BOX, "Quito", "Manta")
        )

        `when`(packageService.updateStatusByTrackingId(trackingId, request)).thenReturn(eventEntity)
        `when`(packageEventMapper.toResponse(eventEntity)).thenReturn(eventResponse)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$baseUrl/$trackingId/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value(Status.IN_TRANSIT.name) }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_event_history_when_get_events() {
        val trackingId = "ABC123"
        val history = PackageEventHistoryResponse(
            `package` = PackageSummaryResponse(1L, trackingId, Type.DOCUMENT, "Cuenca", "Ambato"),
            events = listOf()
        )

        `when`(packageService.getPackageWithEventsByTrackingId(trackingId)).thenReturn(history)

        val result = mockMvc.get("$baseUrl/$trackingId/events")
            .andExpect {
                status { isOk() }
                jsonPath("$.package.trackingId") { value(trackingId) }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @TestConfiguration
    class PackageTestConfig {
        @Bean
        fun packageService(): PackageService = mock(PackageService::class.java)

        @Bean
        fun packageMapper(): PackageMapper = mock(PackageMapper::class.java)

        @Bean
        fun packageEventMapper(): PackageEventMapper = mock(PackageEventMapper::class.java)
    }
}
