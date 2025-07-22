package com.pucetec.pavon_alexander_shipflow.models.entities

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "package_events")
data class PackageEvent (
    val status: Status,

    val comment: String? = null,

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    val packageEntity: Package
): BaseEntity()

enum class Status(val statusName: String){
    PENDING("P"),
    IN_TRANSIT("IT"),
    DELIVERED("D"),
    ON_HOLD("OH"),
    CANCELLED("C"),
}