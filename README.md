# 💼 FinDesk: Premium Multi-Brokerage Portfolio Consolidator

[![Android Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white&style=flat-squared)](#)
[![Kotlin Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white&style=flat-squared)](#)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=pack&logoColor=white&style=flat-squared)](#)
[![Room Database](https://img.shields.io/badge/Database-Room-0052CC?logo=sqlite&logoColor=white&style=flat-squared)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-F1502F?style=flat-squared)](#)

**FinDesk** is a premium, offline-first, non-custodial multi-brokerage wealth consolidator and financial analytics dashboard. 

In today's ecosystem, retail investors split capital across multiple broker accounts (for stocks, mutual funds, or direct equity SIPs), leading to fragmented visibility. FinDesk aggregates multiple accounts, analyzes capital gains/losses dynamically, and delivers an **intelligent financial news feed** curated in real-time by your portfolio's actual asset weight distributions.

---

## 🌟 Visual Showcase & Key Features

### 🏦 1. Multi-Brokerage Aggregation
* **Unified Net Worth Ledger**: View aggregated balances from various independent trading accounts side-by-side.
* **Liquid Margin Tracker**: Instantly analyze cash limits and margin buffers across different linked accounts.
* **Easy Switching**: Effortlessly add new broker connections or switch focus between different corporate or personal portfolios.

### 📊 2. Deep Portfolio Analytics & Tax-Harvesting
* **Asset Allocation Matrix**: Dynamic visual representation of holdings divided into **Equities (Stocks)**, **Mutual Funds (SIPs)**, and **Cash & Money Market Instruments**.
* **Direct Equity Analyzer**: Detailed breakdowns showing total market valuation, total cost basis, and unrealized returns for individual holdings.
* **Tax-Loss Harvesting Indicator**: Displays short-term capital losses and active opportunities to tax-harvest to offset tax liabilities.

### 📰 3. Asset-Curated News Feed ("Smart Curator")
* **Portfolio-Sensitive Headlines**: FinDesk pulls financial headlines via a secure API and dynamically re-prioritizes them using your highest portfolio exposure.
  * *Stocks Dominant*: Prioritizes micro/macro equity indices and corporate earnings related directly to your largest stock holding (e.g., Reliance Industries).
  * *Mutual Funds Dominant*: Elevates Systematic Investment Plan (SIP) records, active vs. passive inflow analysis, and sector rotation reports.
  * *Cash Dominant*: Prioritizes liquid yield alerts, short-term money market indicators, and fixed-income security comparison pieces.
* **Personal Clipboard**: Bookmark critical articles, filter feeds dynamically by asset type, and view calculated read-times.

### ⚡ 4. Real-time Telemetry & Diagnostics
* **Live Instrumentation**: Track memory footprints, database transaction latencies, and active cache statuses inside the diagnostics control panel.

---

## 💎 The FinDesk Advantage: Why Use It?

| Traditional Wealth Trackers | 💼 **FinDesk Platform** |
| :--- | :--- |
| **Data Scraping**: Asks for sensitive bank passwords or SMS access, creating major security risks. | **Non-Custodial**: Operates strictly client-side. FinDesk never requests credentials or banking passwords. |
| **Cloud-Only Lock-in**: Slow load times, sync delays, and total breakdown when offline. | **Offline-First Persistence**: Powered by a local **Room SQLite Engine** for instant access even on flights. |
| **Static AI Spam Feed**: Floods your UI with generic clickbait articles unrelated to your positions. | **Smart Curator Engine**: Headlines are filtered in real-time matching the specific weights of your assets. |
| **Intrusive Trackers**: Sells user portfolio weights to third-party advertisers. | **100% Secure**: Uses Android Sandbox isolation. Zero external trackers or telemetry data sharing. |

---

## 🚀 Installation & Getting Started

### Method 1: Installing the Pre-compiled APK (For Users)
1. **Download the APK**: Grab the latest release package (`.apk`) from the GitHub Releases tab.
2. **Enable Unknown Sources**:
   * Navigate to `Settings` > `Security` on your Android device.
   * Toggle **Allow installation of apps from sources other than the Play Store** on.
3. **Install & Run**: Open the downloaded file using your device's File Manager and tap **Install**. Open **FinDesk** from your application drawer!

### Method 2: Building from Source (For Developers)
#### Prerequisites:
* **Android Studio** (Ladybug 2024.2.1 or newer recommended)
* **Android SDK** Level 34+
* **JDK 17+** configured in your shell environment

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/findesk.git
cd findesk

# 2. Build the debug application package (APK)
./gradlew assembleDebug

# 3. Deploy onto a running physical device or emulator
./gradlew installDebug
```

---

## 🎮 How to Use FinDesk: A Step-by-Step Guide

### Step 1: Initialize Your Portfolio
Upon opening **FinDesk** for the first time, a secure seed transaction initializes default high-quality demonstration portfolios (e.g., "Retirement Core" and "Active Trading") in your local Room database. 

### Step 2: Aggregating Multiple Brokerages
1. Click on **Broker Accounts** in the side navigation drawer.
2. Review linked accounts or click **Link Broker Account** (FAB) to securely aggregate a mock representation of your external account balance.

### Step 3: Check Your Curated Intelligence
1. Navigate back to the main **Dashboard**.
2. Scroll to the bottom to view the **Curated Headlines** section.
3. Change your portfolio's asset mix (e.g., by adding a large stock purchase or cash buffer).
4. Watch the financial intelligence algorithm instantly adapt its feed content focus and tag priorities dynamically!
5. Tap the **Bookmark icon** to flag vital read pieces for later review.

---

## 📂 Architecture & Technical Overview

FinDesk follows the strict guidelines of **Unidirectional Data Flow (UDF)** and **MVVM (Model-View-ViewModel)** Architecture:

* **Presentation Layer**: Built completely in **Jetpack Compose (Material 3)**. Handles orientation changes, dynamic sizing (Adaptive layout support), and edge-to-edge system navigation.
* **State Management**: Orchestrated via a central `WealthViewModel` utilizing modern Kotlin Coroutines and asynchronous state streams (`StateFlow` via `stateIn`).
* **Repository Layer**: Coordinates caching and network fetches. Integrates secure failover capabilities to ensure a flawless presentation even when offline.
* **Data Layer**: Direct SQLite interfacing via Room persistence, strictly using isolated queries for transaction integrity.

---

## 🛡️ License

Distributed under the MIT License. See `LICENSE` for more information.
* Developed by ASMIT SRIVASTAVA (@heyasmit) utilizing the Google AI Studio platform.
<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/914e9926-05c0-4835-abbe-f06d328b9c1c

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
