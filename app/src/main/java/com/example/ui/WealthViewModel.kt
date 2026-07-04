package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BrokerAccountEntity
import com.example.data.HoldingEntity
import com.example.data.PortfolioEntity
import com.example.data.WealthRepository
import com.example.data.PlaceholderNewsApi
import com.example.data.NewsState
import com.example.data.NewsArticle
import com.example.data.NewsCurator
import com.example.data.PlaceholderPost
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class WealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WealthRepository = WealthRepository(AppDatabase.getDatabase(application).wealthDao())
    private val newsApi = PlaceholderNewsApi.create()

    private val _newsState = MutableStateFlow<NewsState>(NewsState.Loading)
    val newsState: StateFlow<NewsState> = _newsState.asStateFlow()

    // List of all portfolios available
    val portfolios: StateFlow<List<PortfolioEntity>> = repository.portfolios.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Portfolio ID
    private val _selectedPortfolioId = MutableStateFlow<String>("")
    val selectedPortfolioId = _selectedPortfolioId.asStateFlow()

    // Active Portfolio details
    private val _activePortfolio = MutableStateFlow<PortfolioEntity?>(null)
    val activePortfolio = _activePortfolio.asStateFlow()

    // Loading indicator for background aggregator sync
    private val _isRefreshingAll = MutableStateFlow(false)
    val isRefreshingAll = _isRefreshingAll.asStateFlow()

    // Currently refreshing broker accounts (for simulating aggregator refresh)
    private val _refreshingAccounts = MutableStateFlow<Set<String>>(emptySet())
    val refreshingAccounts = _refreshingAccounts.asStateFlow()

    // Tax calculator state
    private val _shortTermGainsInput = MutableStateFlow("150000")
    val shortTermGainsInput = _shortTermGainsInput.asStateFlow()

    private val _shortTermLossesInput = MutableStateFlow("45000")
    val shortTermLossesInput = _shortTermLossesInput.asStateFlow()

    // Dynamic light/dark theme state (default to dark mode)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Reactive flow of broker accounts based on selected portfolio
    val brokerAccounts: StateFlow<List<BrokerAccountEntity>> = _selectedPortfolioId
        .flatMapLatest { portfolioId ->
            if (portfolioId.isEmpty()) flowOf(emptyList())
            else repository.getBrokerAccounts(portfolioId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reactive flow of holdings based on selected portfolio
    val holdings: StateFlow<List<HoldingEntity>> = _selectedPortfolioId
        .flatMapLatest { portfolioId ->
            if (portfolioId.isEmpty()) flowOf(emptyList())
            else repository.getHoldings(portfolioId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Run seeding first
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            // Set initial selection
            portfolios.collect { list ->
                if (list.isNotEmpty() && _selectedPortfolioId.value.isEmpty()) {
                    _selectedPortfolioId.value = list.first().id
                    _activePortfolio.value = list.first()
                }
            }
        }

        // Collect holdings and fetch curated headlines based on top asset classes
        viewModelScope.launch {
            holdings.collect { currentHoldings ->
                fetchNewsForHoldings(currentHoldings)
            }
        }
    }

    fun selectPortfolio(portfolioId: String) {
        _selectedPortfolioId.value = portfolioId
        viewModelScope.launch {
            portfolios.value.find { it.id == portfolioId }?.let {
                _activePortfolio.value = it
            }
        }
    }

    fun createPortfolio(name: String) {
        if (name.isBlank()) return
        val id = "portfolio_" + UUID.randomUUID().toString().take(6)
        viewModelScope.launch {
            repository.insertPortfolio(PortfolioEntity(id, name))
            selectPortfolio(id)
        }
    }

    fun connectNewBrokerAccount(name: String, balance: Double, accountNo: String) {
        val portfolioId = _selectedPortfolioId.value
        if (portfolioId.isEmpty() || name.isBlank()) return

        val id = "${portfolioId}_${name.lowercase()}_${UUID.randomUUID().toString().take(4)}"
        viewModelScope.launch {
            // Insert Broker Account
            val newAccount = BrokerAccountEntity(
                id = id,
                portfolioId = portfolioId,
                name = name,
                status = "Synced",
                lastUpdated = System.currentTimeMillis(),
                balance = balance,
                accountNo = accountNo
            )
            repository.insertBrokerAccount(newAccount)

            // Auto generate some holdings based on balance to populate charts
            // 60% stocks, 30% mutual funds, 10% cash
            val stocksVal = balance * 0.60
            val mfVal = balance * 0.30
            val cashVal = balance * 0.10

            repository.insertHolding(
                HoldingEntity(0, portfolioId, id, "STOCKS", "$name Bluechip Portfolio", stocksVal)
            )
            repository.insertHolding(
                HoldingEntity(0, portfolioId, id, "MUTUAL_FUNDS", "$name Hybrid Growth Fund", mfVal, 12.5, "HDFC Bank, Reliance")
            )
            repository.insertHolding(
                HoldingEntity(0, portfolioId, id, "CASH", "Broker Margin Account", cashVal)
            )
        }
    }

    fun refreshBrokerAccount(accountId: String) {
        viewModelScope.launch {
            _refreshingAccounts.value = _refreshingAccounts.value + accountId
            delay(1500) // Realistic network delay simulation
            
            // Fetch account, update lastUpdated and set status back to Synced
            // We find it from our existing list
            brokerAccounts.value.find { it.id == accountId }?.let { account ->
                val updatedAccount = account.copy(
                    lastUpdated = System.currentTimeMillis(),
                    status = "Synced"
                )
                repository.insertBrokerAccount(updatedAccount)
            }
            _refreshingAccounts.value = _refreshingAccounts.value - accountId
        }
    }

    fun refreshAllAccounts() {
        val portfolioId = _selectedPortfolioId.value
        if (portfolioId.isEmpty()) return

        viewModelScope.launch {
            _isRefreshingAll.value = true
            // Mark all refreshing
            val accounts = brokerAccounts.value
            accounts.forEach { account ->
                _refreshingAccounts.value = _refreshingAccounts.value + account.id
            }

            delay(2000) // Simulate unified Account Aggregator fetching

            accounts.forEach { account ->
                val updatedAccount = account.copy(
                    lastUpdated = System.currentTimeMillis(),
                    status = "Synced"
                )
                repository.insertBrokerAccount(updatedAccount)
            }
            _refreshingAccounts.value = emptySet()
            _isRefreshingAll.value = false
        }
    }

    fun updateTaxSTCG(gains: String) {
        _shortTermGainsInput.value = gains.filter { it.isDigit() }
    }

    fun updateTaxLosses(losses: String) {
        _shortTermLossesInput.value = losses.filter { it.isDigit() }
    }

    fun fetchNewsForHoldings(currentHoldings: List<HoldingEntity>) {
        viewModelScope.launch {
            _newsState.value = NewsState.Loading
            
            val stocksTotal = currentHoldings.filter { it.assetType == "STOCKS" }.sumOf { it.value }
            val mfTotal = currentHoldings.filter { it.assetType == "MUTUAL_FUNDS" }.sumOf { it.value }
            val cashTotal = currentHoldings.filter { it.assetType == "CASH" }.sumOf { it.value }
            
            val sortedAssetClasses = listOf(
                "STOCKS" to stocksTotal,
                "MUTUAL_FUNDS" to mfTotal,
                "CASH" to cashTotal
            ).sortedByDescending { it.second }
             .filter { it.second > 0.0 || it.first == "STOCKS" }
             .map { it.first }

            val topStock = currentHoldings.filter { it.assetType == "STOCKS" }.maxByOrNull { it.value }?.name
                ?: "Reliance Industries"

            try {
                val posts = newsApi.getPosts(10)
                val curated = NewsCurator.curate(posts, sortedAssetClasses, topStock)
                _newsState.value = NewsState.Success(curated)
            } catch (e: Exception) {
                // Secure zero-delay offline fallback if there are network issues
                val fallbackPosts = List(10) { i ->
                    PlaceholderPost(userId = 1, id = i + 1, title = "Mock Title", body = "Mock Body")
                }
                val curated = NewsCurator.curate(fallbackPosts, sortedAssetClasses, topStock)
                _newsState.value = NewsState.Success(curated)
            }
        }
    }

    fun toggleBookmark(articleId: Int) {
        val currentState = _newsState.value
        if (currentState is NewsState.Success) {
            val updated = currentState.articles.map {
                if (it.id == articleId) it.copy(isBookmarked = !it.isBookmarked) else it
            }
            _newsState.value = NewsState.Success(updated)
        }
    }
}
