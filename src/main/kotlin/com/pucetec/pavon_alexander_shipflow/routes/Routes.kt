package com.pucetec.pavon_alexander_shipflow.routes

object Routes {
    const val BASE_URL = "/api/shipflow"

    // PACKAGES
    const val PACKAGES = "$BASE_URL/packages"
    const val PACKAGE_BY_ID = "$PACKAGES/{id}"
    const val PACKAGE_STATUS_UPDATE = "$PACKAGES/{id}/status"
    const val PACKAGE_EVENTS = "$PACKAGES/{id}/events"
}