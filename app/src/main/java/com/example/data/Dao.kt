package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WealthDao {
    // Portfolios
    @Query("SELECT * FROM portfolios")
    fun getPortfolios(): Flow<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: PortfolioEntity)

    // Broker Accounts
    @Query("SELECT * FROM broker_accounts WHERE portfolioId = :portfolioId")
    fun getBrokerAccountsForPortfolio(portfolioId: String): Flow<List<BrokerAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrokerAccount(account: BrokerAccountEntity)

    @Update
    suspend fun updateBrokerAccount(account: BrokerAccountEntity)

    @Query("DELETE FROM broker_accounts WHERE id = :accountId")
    suspend fun deleteBrokerAccount(accountId: String)

    // Holdings
    @Query("SELECT * FROM holdings WHERE portfolioId = :portfolioId")
    fun getHoldingsForPortfolio(portfolioId: String): Flow<List<HoldingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE portfolioId = :portfolioId AND brokerId = :brokerId")
    suspend fun deleteHoldingsForBroker(portfolioId: String, brokerId: String)

    @Query("DELETE FROM holdings WHERE id = :holdingId")
    suspend fun deleteHolding(holdingId: Int)
}
