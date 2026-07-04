package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WealthRepository(private val wealthDao: WealthDao) {

    val portfolios: Flow<List<PortfolioEntity>> = wealthDao.getPortfolios()

    fun getBrokerAccounts(portfolioId: String): Flow<List<BrokerAccountEntity>> {
        return wealthDao.getBrokerAccountsForPortfolio(portfolioId)
    }

    fun getHoldings(portfolioId: String): Flow<List<HoldingEntity>> {
        return wealthDao.getHoldingsForPortfolio(portfolioId)
    }

    suspend fun insertPortfolio(portfolio: PortfolioEntity) {
        wealthDao.insertPortfolio(portfolio)
    }

    suspend fun insertBrokerAccount(account: BrokerAccountEntity) {
        wealthDao.insertBrokerAccount(account)
    }

    suspend fun updateBrokerAccount(account: BrokerAccountEntity) {
        wealthDao.updateBrokerAccount(account)
    }

    suspend fun deleteBrokerAccount(accountId: String) {
        wealthDao.deleteBrokerAccount(accountId)
    }

    suspend fun insertHolding(holding: HoldingEntity) {
        wealthDao.insertHolding(holding)
    }

    suspend fun deleteHoldingsForBroker(portfolioId: String, brokerId: String) {
        wealthDao.deleteHoldingsForBroker(portfolioId, brokerId)
    }

    suspend fun checkAndSeedDatabase() {
        val existingPortfolios = wealthDao.getPortfolios().first()
        if (existingPortfolios.isEmpty()) {
            // 1. Seed Portfolios
            val p1 = PortfolioEntity("p1", "Main Family Wealth")
            val p2 = PortfolioEntity("p2", "Retirement Goal")
            val p3 = PortfolioEntity("p3", "High-Beta Speculative")

            wealthDao.insertPortfolio(p1)
            wealthDao.insertPortfolio(p2)
            wealthDao.insertPortfolio(p3)

            // 2. Seed Broker Accounts for Portfolio 1 (Main Family)
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p1_zerodha", "p1", "Zerodha", "Synced", System.currentTimeMillis() - 45 * 60000, 8250000.0, "ZER194827")
            )
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p1_groww", "p1", "Groww", "Synced", System.currentTimeMillis() - 15 * 60000, 4500000.0, "GRW882390")
            )
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p1_upstox", "p1", "Upstox", "Synced", System.currentTimeMillis() - 120 * 60000, 1500000.0, "UPX001928")
            )

            // Seed Holdings for Portfolio 1
            // Stocks (Zerodha)
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_zerodha", "STOCKS", "Reliance Industries Ltd.", 2500000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_zerodha", "STOCKS", "HDFC Bank Ltd.", 2200000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_zerodha", "STOCKS", "Infosys Ltd.", 1800000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_zerodha", "STOCKS", "Tata Motors Ltd.", 1000000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_zerodha", "STOCKS", "Tata Consultancy Services", 750000.0))

            // Mutual Funds (Groww)
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_groww", "MUTUAL_FUNDS", "Parag Parikh Flexi Cap Fund", 2500000.0, 68.5, "HDFC Bank Ltd., Reliance Industries Ltd."))
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_groww", "MUTUAL_FUNDS", "SBI Bluechip Direct Growth", 2000000.0, 42.0, "Reliance Industries Ltd., Infosys Ltd."))

            // Cash / Liquid (Upstox)
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_upstox", "CASH", "HDFC Liquid Fund Direct", 1000000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p1", "p1_upstox", "CASH", "Uninvested Cash Margin", 500000.0))


            // 3. Seed Broker Accounts for Portfolio 2 (Retirement Goal)
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p2_zerodha", "p2", "Zerodha", "Synced", System.currentTimeMillis() - 3 * 3600000, 3500000.0, "ZER773822")
            )
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p2_groww", "p2", "Groww", "Synced", System.currentTimeMillis() - 5 * 3600000, 2500000.0, "GRW229381")
            )
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p2_upstox", "p2", "Upstox", "Synced", System.currentTimeMillis() - 8 * 3600000, 500000.0, "UPX482711")
            )

            // Seed Holdings for Portfolio 2
            wealthDao.insertHolding(HoldingEntity(0, "p2", "p2_zerodha", "STOCKS", "Larsen & Toubro Ltd.", 1200000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p2", "p2_zerodha", "STOCKS", "ICICI Bank Ltd.", 1300000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p2", "p2_zerodha", "STOCKS", "ITC Ltd.", 1000000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p2", "p2_groww", "MUTUAL_FUNDS", "HDFC Mid-Cap Opportunities", 1500000.0, 54.0, "ICICI Bank Ltd., Larsen & Toubro Ltd."))
            wealthDao.insertHolding(HoldingEntity(0, "p2", "p2_groww", "MUTUAL_FUNDS", "Mirae Asset Large Cap Fund", 1000000.0, 31.5, "ICICI Bank Ltd."))
            wealthDao.insertHolding(HoldingEntity(0, "p2", "p2_upstox", "CASH", "Liquid Margin", 500000.0))


            // 4. Seed Broker Accounts for Portfolio 3 (High Beta Speculative)
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p3_upstox", "p3", "Upstox", "Synced", System.currentTimeMillis() - 15 * 60000, 1800000.0, "UPX092837")
            )
            wealthDao.insertBrokerAccount(
                BrokerAccountEntity("p3_groww", "p3", "Groww", "Synced", System.currentTimeMillis() - 10 * 60000, 400000.0, "GRW992019")
            )

            // Seed Holdings for Portfolio 3
            wealthDao.insertHolding(HoldingEntity(0, "p3", "p3_upstox", "STOCKS", "Zomato Ltd.", 800000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p3", "p3_upstox", "STOCKS", "Jio Financial Services", 500000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p3", "p3_upstox", "STOCKS", "Tata Electronics Concept", 500000.0))
            wealthDao.insertHolding(HoldingEntity(0, "p3", "p3_groww", "MUTUAL_FUNDS", "Quant Small Cap Fund", 400000.0, 15.0, "Zomato Ltd."))
        }
    }
}
