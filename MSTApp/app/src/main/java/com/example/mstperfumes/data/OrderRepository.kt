package com.example.mstperfumes.data

object OrderRepository {
    const val STATUS_PLACED = "Order Placed"
    const val STATUS_PREPARING = "Preparing"
    const val STATUS_IN_TRANSIT = "In Transit"
    const val STATUS_DELIVERED = "Delivered"

    var currentStatus: String = STATUS_IN_TRANSIT
        private set

    fun updateStatus(newStatus: String) {
        currentStatus = newStatus
    }

    fun getStatusStepIndex(): Int {
        return when (currentStatus) {
            STATUS_PLACED -> 0
            STATUS_PREPARING -> 1
            STATUS_IN_TRANSIT -> 2
            STATUS_DELIVERED -> 3
            else -> 0
        }
    }
}
