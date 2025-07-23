package com.pucetec.pavon_alexander_shipflow.models.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "packages")
data class Package (
    val type: Type,
    val weight: Float,
    @Column(length = 50)
    val description: String,
    @Column(name = "city_from")
    val cityFrom: String,
    @Column(name = "city_to")
    val cityTo: String,

    @Column(name = "tracking_id", unique = true, nullable = false)
    val trackingId: String = UUID.randomUUID().toString(),

    @Column(name = "estimated_delivery_date")
    val estimatedDeliveryDate: LocalDateTime = LocalDateTime.now().plusDays(5),

    @OneToMany(mappedBy = "packageEntity", cascade = [CascadeType.ALL])
    val events: List<PackageEvent> = listOf()

): BaseEntity()

enum class Type(name: String){
    DOCUMENT("D"),
    SMALL_BOX("SB"),
    FRAGILE("F")
}
