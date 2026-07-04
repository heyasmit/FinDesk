package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolios")
data class PortfolioEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "broker_accounts")
data class BrokerAccountEntity(
    @PrimaryKey val id: String,
    val portfolioId: String,
    val name: String,
    val status: String, // "Synced", "Refreshing", "Failed"
    val lastUpdated: Long,
    val balance: Double,
    val accountNo: String
)

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val portfolioId: String,
    val brokerId: String,
    val assetType: String, // "STOCKS", "MUTUAL_FUNDS", "CASH"
    val name: String,
    val value: Double,
    val overlapPercentage: Double = 0.0, // Used for mutual funds overlapping with others
    val overlappingStocks: String = "" // Comma-separated list of stock names
)
