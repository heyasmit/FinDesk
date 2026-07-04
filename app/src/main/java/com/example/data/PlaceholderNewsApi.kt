package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceholderNewsApi {
    @GET("posts")
    suspend fun getPosts(@Query("_limit") limit: Int = 12): List<PlaceholderPost>

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        fun create(): PlaceholderNewsApi {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(PlaceholderNewsApi::class.java)
        }
    }
}

data class PlaceholderPost(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

data class NewsArticle(
    val id: Int,
    val title: String,
    val summary: String,
    val assetType: String, // "STOCKS", "MUTUAL_FUNDS", "CASH"
    val category: String,
    val source: String,
    val timeAgo: String,
    val readTime: String,
    val isBookmarked: Boolean = false
)

object NewsCurator {
    fun curate(
        posts: List<PlaceholderPost>,
        topAssetClasses: List<String>,
        topStockName: String? = null
    ): List<NewsArticle> {
        val assetPriority = topAssetClasses.ifEmpty { listOf("STOCKS", "MUTUAL_FUNDS", "CASH") }
        
        return posts.mapIndexed { index, post ->
            val assetType = when {
                assetPriority.size >= 3 -> {
                    val r = index % 10
                    if (r < 5) assetPriority[0]
                    else if (r < 8) assetPriority[1]
                    else assetPriority[2]
                }
                assetPriority.size == 2 -> {
                    if (index % 2 == 0) assetPriority[0] else assetPriority[1]
                }
                assetPriority.size == 1 -> {
                    assetPriority[0]
                }
                else -> "STOCKS"
            }
            
            val templateIndex = (post.id + index) % 4
            
            val (titleTemplate, summaryTemplate, category, source) = when (assetType) {
                "STOCKS" -> {
                    when (templateIndex) {
                        0 -> Quadruple(
                            "Equity Index Breaks Key Resistance; Bull Run Sustained",
                            "Market indices touched new heights today driven by aggressive domestic institutional flows and strong earnings across high-beta and tech stocks like {STOCK}.",
                            "Market Alert",
                            "FinDesk Intelligence"
                        )
                        1 -> Quadruple(
                            "Large-Cap Tech Stocks See Historic Accumulation by Pension Funds",
                            "Heavyweights like {STOCK} saw significant volume surges as major pension funds increased their direct equity allocations.",
                            "Direct Equity",
                            "Alpha Insights"
                        )
                        2 -> Quadruple(
                            "Automotive & EV Supply Chains Drive Manufacturing Outperformance",
                            "New policy incentives have triggered a massive wave of capital expenditure in sectors related to {STOCK}, pushing valuations to premium levels.",
                            "Sector Watch",
                            "Business Standards"
                        )
                        else -> Quadruple(
                            "Why Long-Term Investors Are Shifting Focus to High-Moat Bluechips",
                            "Value investing makes a strong comeback as market volatility increases. Advisors recommend focusing on strong balance sheets like {STOCK}.",
                            "Investment Strategy",
                            "Wealth Digest"
                        )
                    }
                }
                "MUTUAL_FUNDS" -> {
                    when (templateIndex) {
                        0 -> Quadruple(
                            "SIP Registrations Hit Record High of 10 Million Monthly Accounts",
                            "Retail investors are embracing disciplined, long-term wealth compounding, providing a resilient buffer to capital markets.",
                            "Fund Flows",
                            "Economic Times"
                        )
                        1 -> Quadruple(
                            "Active Flexi-Cap Funds Outperform Passive Index Trackers",
                            "Fund managers who tactically adjusted weightings in financial services and IT during recent sector rotations delivered superior alpha.",
                            "Fund Analysis",
                            "MorningStar Review"
                        )
                        2 -> Quadruple(
                            "NFO Boom: Liquid and Dynamic Allocation Funds Attract Heavy Capital",
                            "New Fund Offers are seeing enthusiastic investor response as families seek balanced volatility-adjusted returns.",
                            "New Launches",
                            "Mutual Fund Insights"
                        )
                        else -> Quadruple(
                            "Understanding the Portfolio Overlap Risk in Your Multi-Cap Funds",
                            "Recent analytical reports warn that holding more than four equity funds often leads to unintentional portfolio duplication without added diversification.",
                            "Risk Management",
                            "FinDesk X-Ray Desk"
                        )
                    }
                }
                else -> { // CASH
                    when (templateIndex) {
                        0 -> Quadruple(
                            "Liquid Funds Yields Edge Higher as Money Market Rates Firm Up",
                            "With short-term rates remaining stable, parking surplus capital in high-yield liquid funds offers robust risk-free returns.",
                            "Fixed Income",
                            "Bond Market Wire"
                        )
                        1 -> Quadruple(
                            "Is Your Cash Cushion Too Large? Optimizing Emergency Reserves",
                            "Financial planners suggest that holding excess cash in non-yielding accounts drags down overall portfolio CAGR.",
                            "Cash Management",
                            "Smart Money Advisor"
                        )
                        2 -> Quadruple(
                            "Central Bank Signals Stable Policy Rate Corridor Through Next Quarter",
                            "Monetary policy committee maintains stance, keeping short-term cash yields predictable and attractive for capital preservation.",
                            "Macro Economy",
                            "Reserve Desk"
                        )
                        else -> Quadruple(
                            "Treasury Bills vs. Liquid Reserves: Where to Park Capital",
                            "A detailed comparison of yield-to-maturity (YTM) for ultra-short term sovereign papers versus liquid brokerage margin accounts.",
                            "Treasury Spot",
                            "Bloomberg Quint"
                        )
                    }
                }
            }
            
            val stockReplacement = topStockName ?: "Reliance Industries"
            val title = titleTemplate.replace("{STOCK}", stockReplacement)
            val summary = summaryTemplate.replace("{STOCK}", stockReplacement)
            
            val minutes = (index * 15 + 5)
            val timeAgo = if (minutes < 60) "${minutes}m ago" else "${minutes / 60}h ago"
            val readTime = "${(index % 3) + 2} min read"
            
            NewsArticle(
                id = post.id,
                title = title,
                summary = summary,
                assetType = assetType,
                category = category,
                source = source,
                timeAgo = timeAgo,
                readTime = readTime,
                isBookmarked = false
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

sealed class NewsState {
    object Loading : NewsState()
    data class Success(val articles: List<NewsArticle>) : NewsState()
    data class Error(val message: String) : NewsState()
}
