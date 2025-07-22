package com.pucetec.pavon_alexander_shipflow.repositories

import com.pucetec.pavon_alexander_shipflow.models.entities.PackageEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PackageEventRepository: JpaRepository<PackageEvent, Long>