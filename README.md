# Dhan Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.sonicalgo/dhan-java-sdk)](https://central.sonatype.com/artifact/io.github.sonicalgo/dhan-java-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)

Unofficial Kotlin/Java SDK for the [Dhan](https://dhanhq.co) trading platform. Supports REST APIs and real-time WebSocket streaming.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.sonicalgo:dhan-java-sdk:1.0.0")
```

### Gradle (Groovy)

```groovy
implementation 'io.github.sonicalgo:dhan-java-sdk:1.0.0'
```

### Maven

```xml
<dependency>
    <groupId>io.github.sonicalgo</groupId>
    <artifactId>dhan-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

<details open>
<summary>Kotlin</summary>

```kotlin
import io.github.sonicalgo.dhan.Dhan
import io.github.sonicalgo.dhan.model.request.*
import io.github.sonicalgo.dhan.model.enums.*

// Create SDK instance
val dhan = Dhan.builder()
    .clientId("your-client-id")
    .accessToken("your-access-token")
    .build()

// Get user profile
val profile = dhan.getAuthApi().getProfile()
println("Client: ${profile.dhanClientId}")

// Get market quote
val ltps = dhan.getMarketQuoteApi().getLtp(mapOf(
    "NSE_EQ" to listOf(1333)  // HDFC Bank
))
println("LTP: ${ltps["NSE_EQ"]?.get("1333")?.lastPrice}")

// Place an order
val response = dhan.getOrdersApi().placeOrder(PlaceOrderParams(
    transactionType = TransactionType.BUY,
    exchangeSegment = ExchangeSegment.NSE_EQ,
    productType = ProductType.CNC,
    orderType = OrderType.LIMIT,
    validity = Validity.DAY,
    securityId = "1333",
    quantity = 10,
    price = 1428.0
))
println("Order ID: ${response.orderId}")
```

</details>

<details>
<summary>Java</summary>

```java
import io.github.sonicalgo.dhan.Dhan;
import io.github.sonicalgo.dhan.model.request.*;
import io.github.sonicalgo.dhan.model.enums.*;
import java.util.Map;
import java.util.List;

// Create SDK instance
Dhan dhan = Dhan.builder()
    .clientId("your-client-id")
    .accessToken("your-access-token")
    .build();

// Get user profile
var profile = dhan.getAuthApi().getProfile();
System.out.println("Client: " + profile.getDhanClientId());

// Get market quote
var ltps = dhan.getMarketQuoteApi().getLtp(Map.of(
    "NSE_EQ", List.of(1333)  // HDFC Bank
));
System.out.println("LTP: " + ltps.get("NSE_EQ").get("1333").getLastPrice());

// Place an order
var response = dhan.getOrdersApi().placeOrder(new PlaceOrderParams(
    null,                       // dhanClientId (auto-injected)
    null,                       // correlationId
    TransactionType.BUY,
    ExchangeSegment.NSE_EQ,
    ProductType.CNC,
    OrderType.LIMIT,
    Validity.DAY,
    "1333",                     // securityId
    10,                         // quantity
    0,                          // disclosedQuantity
    1428.0,                     // price
    0.0,                        // triggerPrice
    false,                      // afterMarketOrder
    null,                       // amoTime
    null,                       // boProfitValue
    null                        // boStopLossValue
));
System.out.println("Order ID: " + response.getOrderId());
```

</details>

> **Note:** All code examples below assume you have initialized the SDK as shown above:
> ```kotlin
> val dhan = Dhan.builder()
>     .clientId("your-client-id")
>     .accessToken("your-access-token")
>     .build()
> ```

## Type-Safe Enums

The SDK uses type-safe enums throughout the API responses for better code safety and IDE support:

```kotlin
// Order response uses enums
val order = dhan.getOrdersApi().getOrderById("order-id")
when (order.orderStatus) {
    OrderStatus.TRADED -> println("Order filled")
    OrderStatus.REJECTED -> println("Order rejected")
    OrderStatus.PENDING -> println("Order pending")
    OrderStatus.PART_TRADED -> println("Partial fill: ${order.filledQty}/${order.quantity}")
    else -> println("Status: ${order.orderStatus}")
}

// Position uses enums
val positions = dhan.getPortfolioApi().getPositions()
positions.filter { it.positionType == PositionType.LONG }
    .forEach { println("Long position: ${it.tradingSymbol}") }
```

**Available enums:**

| Category | Enums |
|----------|-------|
| Trading | `ExchangeSegment`, `TransactionType`, `ProductType`, `OrderType`, `Validity` |
| Order Status | `OrderStatus`, `AmoTime`, `LegName` |
| Forever Orders | `ForeverOrderFlag` |
| Historical Data | `ChartInterval`, `InstrumentType` |
| Portfolio | `PositionType` |
| Options | `DrvOptionType` |
| Auth/Config | `IpFlag`, `KillSwitchStatus`, `DdpiStatus`, `MtfStatus`, `DataPlanStatus` |
| EDIS | `EdisStatus`, `Exchange`, `Segment` |

## Why This SDK?

- **Modern & Secure** - Built with latest libraries (OkHttp 5.x, Jackson 2.x) with no known vulnerabilities
- **WebSocket Ready** - Binary protocol parsing for market feed; no manual binary handling needed
- **Auto-Reconnection** - WebSocket clients automatically reconnect with exponential backoff
- **Simple API** - Clean builder pattern: `Dhan.builder().clientId("id").accessToken("token").build()`
- **Type-Safe** - Kotlin data classes with proper types; no raw Maps or Object casting
- **Rich Error Handling** - Exceptions with helpers like `isRateLimitError`, `isAuthenticationError`
- **Thread-Safe** - Designed for concurrent usage in trading applications
- **Resource Management** - Implements `Closeable` for clean resource cleanup

## Features

- **13 REST API modules** - Orders, Portfolio, Market Quotes, Historical Data, Option Chain, and more
- **Real-time market data** - WebSocket streaming with binary protocol (low latency)
- **Real-time order updates** - Order and trade updates via WebSocket
- **Automatic reconnection** - WebSocket clients reconnect with exponential backoff
- **Configurable rate limiting** - Automatic retry with exponential backoff for HTTP 429
- **Debug logging** - Optional HTTP request/response logging for troubleshooting
- **Full Kotlin & Java compatibility** - Use from either language

---

## Configuration

### SDK Configuration

```kotlin
// Configure during initialization using builder pattern
val dhan = Dhan.builder()
    .clientId("your-client-id")       // Required
    .accessToken("your-access-token") // Optional at build, can set later
    .loggingEnabled(true)             // Enable HTTP request/response logging
    .rateLimitRetries(3)              // Configure rate limit retry (0-5 attempts)
    .build()
```

| Setting | Builder Method | Default | Range | Description |
|---------|----------------|---------|-------|-------------|
| Client ID | `clientId(String)` | - | - | Your Dhan client ID (required) |
| Access Token | `accessToken(String)` | `""` | - | OAuth access token (can set later) |
| HTTP Logging | `loggingEnabled(Boolean)` | `false` | - | Log HTTP requests/responses for debugging |
| Rate Limit Retry | `rateLimitRetries(Int)` | `0` | 0-5 | Auto-retry on HTTP 429 with exponential backoff |

> **Note:** When `rateLimitRetries > 0`, the SDK automatically retries rate-limited requests (HTTP 429) with exponential backoff (1s, 2s, 4s, ...) before throwing an exception.

### WebSocket Configuration

WebSocket reconnection settings are configured per-client during creation:

```kotlin
// Market Data Feed Client
val feedClient = dhan.createMarketFeedClient(
    maxReconnectAttempts = 10,      // Default: 5, Max reconnection attempts
    autoReconnectEnabled = true,    // Default: true, Auto-reconnect on disconnect
    autoResubscribeEnabled = true   // Default: true, Auto-resubscribe after reconnect
)

// Order Update Client
val orderClient = dhan.createOrderUpdateClient(
    maxReconnectAttempts = 10,   // Default: 5
    autoReconnectEnabled = true  // Default: true
)
```

### Timeouts

| Setting | Default |
|---------|---------|
| Connect timeout | 10 seconds |
| Read timeout | 30 seconds |
| Write timeout | 30 seconds |

### WebSocket Settings

| Setting | Default |
|---------|---------|
| Ping interval | 10 seconds |
| Initial reconnect delay | 1 second |
| Max reconnect delay | 30 seconds |
| Max reconnect attempts | 5 (configurable 1-20) |
| Max instruments per connection | 5,000 |
| Max instruments per subscription | 100 |
| Max connections per user | 5 |

### Base URLs

| Endpoint | URL |
|----------|-----|
| REST API v2 | `https://api.dhan.co/v2` |
| Auth | `https://auth.dhan.co` |
| Instruments | `https://images.dhan.co/api-data` |
| Market Feed WebSocket | `wss://api-feed.dhan.co` |
| Order Update WebSocket | `wss://api-order-update.dhan.co` |

## Table of Contents

- [Configuration](#configuration)
- [WebSocket Streaming](#websocket-streaming)
  - [Market Data Feed](#market-data-feed)
  - [Order Updates](#order-updates)
  - [Token Refresh with WebSocket](#token-refresh-with-websocket)
- [REST API Reference](#rest-api-reference)
  - [Authentication](#authentication)
  - [Orders](#orders)
  - [Super Orders](#super-orders)
  - [Forever Orders (GTT)](#forever-orders-gtt)
  - [Portfolio](#portfolio)
  - [Funds & Margins](#funds--margins)
  - [Market Quotes](#market-quotes)
  - [Historical Data](#historical-data)
  - [Option Chain](#option-chain)
  - [Instruments](#instruments)
  - [EDIS](#edis)
  - [Traders Control](#traders-control)
  - [Statement](#statement)
- [Error Handling](#error-handling)
- [Resource Management](#resource-management)
- [Requirements](#requirements)
- [License](#license)

---

## WebSocket Streaming

### Market Data Feed

Real-time market data via WebSocket with binary protocol encoding (low latency). Prices are formatted as String with 2 decimal places (e.g., `"1428.50"`).

```kotlin
import io.github.sonicalgo.dhan.model.ws.*
import io.github.sonicalgo.dhan.websocket.marketfeed.*

// Create client with custom reconnection settings (optional)
val feedClient = dhan.createMarketFeedClient(
    maxReconnectAttempts = 10,      // Default: 5
    autoReconnectEnabled = true,    // Default: true
    autoResubscribeEnabled = true   // Default: true, auto-resubscribe after reconnect
)

// Add listener
feedClient.addListener(object : MarketFeedListener {
    override fun onConnected(isReconnect: Boolean) {
        if (isReconnect) {
            println("Reconnected! Subscriptions restored.")
        } else {
            println("Connected to market feed!")
            // Subscribe with TICKER mode
            feedClient.subscribe(
                listOf(
                    Instrument.nseEquity("1333"),   // HDFC Bank
                    Instrument.nseEquity("11536"),  // TCS
                    Instrument.index("26000")       // Nifty 50
                ),
                FeedMode.TICKER
            )
        }
    }

    override fun onTickerData(data: TickerData) {
        // data.ltp is String (e.g., "1428.50")
        println("${data.securityId}: LTP=${data.ltp}")
    }

    override fun onQuoteData(data: QuoteData) {
        println("${data.securityId}: LTP=${data.ltp}, Volume=${data.volume}")
        println("OHLC: O=${data.openPrice} H=${data.highPrice} L=${data.lowPrice} C=${data.closePrice}")
    }

    override fun onFullData(data: FullData) {
        println("${data.securityId}: LTP=${data.ltp}, OI=${data.openInterest}")
        data.bids.forEachIndexed { i, bid ->
            println("  Bid $i: ${bid.quantity} @ ${bid.price}")
        }
    }

    override fun onIndexData(data: IndexData) {
        println("Index ${data.securityId}: ${data.indexValue}")
    }

    override fun onReconnecting(attempt: Int, delayMs: Long) {
        println("Reconnecting (attempt $attempt) in ${delayMs}ms...")
    }

    override fun onDisconnected(code: Int, reason: String) {
        println("Disconnected: $reason")
    }

    override fun onError(error: Throwable) {
        println("Error: ${error.message}")
    }
})

// Connect to WebSocket
feedClient.connect()
```

#### Feed Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| `FeedMode.TICKER` | LTP, LTQ, Volume, OHLC | Minimal bandwidth, price tracking |
| `FeedMode.QUOTE` | Ticker + best 5 bid/ask | Standard trading |
| `FeedMode.FULL` | Quote + Open Interest | F&O trading with OI data |

#### Subscription Management

```kotlin
// Subscribe to instruments
feedClient.subscribe(listOf(Instrument.nseEquity("1333")), FeedMode.TICKER)

// Change mode for specific instruments
feedClient.subscribe(listOf(Instrument.nseEquity("1333")), FeedMode.QUOTE)

// Subscribe to full mode with market depth
feedClient.subscribe(listOf(Instrument.nseFno("43225")), FeedMode.FULL)

// Unsubscribe
feedClient.unsubscribe(listOf(Instrument.nseEquity("11536")))

// Get current subscriptions
val subscriptions = feedClient.getSubscriptions()  // Map<Instrument, FeedMode>
val count = feedClient.subscriptionCount

// Unsubscribe all
feedClient.unsubscribeAll()

// Close connection
feedClient.close()
```

#### Instrument Creation

```kotlin
Instrument.nseEquity("1333")      // NSE stocks
Instrument.nseFno("43225")        // NSE F&O
Instrument.index("26000")         // NIFTY 50
Instrument.mcxCommodity("224035") // MCX instruments
Instrument.bseEquity("532540")    // BSE stocks
Instrument.bseFno("...")          // BSE F&O
Instrument.nseCurrency("...")     // NSE Currency
Instrument.bseCurrency("...")     // BSE Currency
```

### Order Updates

Real-time order and trade updates via WebSocket.

```kotlin
import io.github.sonicalgo.dhan.model.ws.*
import io.github.sonicalgo.dhan.websocket.orderupdate.*

// Create client with custom reconnection settings (optional)
val orderClient = dhan.createOrderUpdateClient(
    maxReconnectAttempts = 10,   // Default: 5
    autoReconnectEnabled = true  // Default: true
)

// Add listener
orderClient.addListener(object : OrderUpdateListener {
    override fun onConnected(isReconnect: Boolean) {
        if (isReconnect) {
            println("Reconnected to order updates")
        } else {
            println("Connected to order updates!")
        }
    }

    override fun onOrderUpdate(update: OrderUpdate) {
        println("Order ${update.orderId}: ${update.orderStatus}")
        when (update.orderStatus) {
            OrderStatus.TRADED -> println("Order fully executed!")
            OrderStatus.REJECTED -> println("Rejected: ${update.omsErrorDescription}")
            OrderStatus.PART_TRADED -> println("Partial fill: ${update.filledQty}/${update.quantity}")
            else -> {}
        }
    }

    override fun onTradeUpdate(update: TradeUpdate) {
        println("Trade: ${update.tradedQuantity} @ ${update.tradedPrice}")
    }

    override fun onConnectionStatus(status: ConnectionStatus) {
        println("Connection status: $status")
    }

    override fun onReconnecting(attempt: Int, delayMs: Long) {
        println("Reconnecting (attempt $attempt) in ${delayMs}ms...")
    }

    override fun onDisconnected(code: Int, reason: String) {
        println("Disconnected: $reason")
    }

    override fun onError(error: Throwable) {
        println("Error: ${error.message}")
    }
})

// Connect
orderClient.connect()

// Close when done
orderClient.close()
```

### Token Refresh with WebSocket

REST API calls automatically use the latest access token. However, WebSocket clients authenticate once at connection time. If you update the token while a WebSocket is connected, reconnect to use the new token:

```kotlin
// Update token
dhan.setAccessToken("new-access-token")

// REST API calls immediately use new token
dhan.getOrdersApi().getOrderBook()  // Uses new token

// WebSocket needs reconnection to use new token
feedClient.close()
feedClient.connect()  // Now authenticates with new token
```

---

## REST API Reference

### Authentication

```kotlin
val authApi = dhan.getAuthApi()

// Get user profile
val profile = authApi.getProfile()
// Returns: dhanClientId, tokenValidity, activeSegments, dataPlan

// Set static IP (required for order APIs)
authApi.setIp(SetIpParams(
    ip = "203.0.113.50",
    ipFlag = IpFlag.PRIMARY
))

// Modify IP (allowed once every 7 days)
authApi.modifyIp(SetIpParams(
    ip = "203.0.113.51",
    ipFlag = IpFlag.SECONDARY
))

// Get IP configuration
val ipConfig = authApi.getIpConfiguration()
println("Primary IP: ${ipConfig.primaryIp}")
println("Secondary IP: ${ipConfig.secondaryIp}")

// Renew token (extends validity for 24 hours)
authApi.renewToken()
```

### Orders

> **Note:** Order placement, modification, and cancellation APIs require static IP whitelisting. Configure your IP at [web.dhan.co](https://web.dhan.co).

#### Place Order

```kotlin
val ordersApi = dhan.getOrdersApi()

val response = ordersApi.placeOrder(PlaceOrderParams(
    transactionType = TransactionType.BUY,
    exchangeSegment = ExchangeSegment.NSE_EQ,
    productType = ProductType.CNC,        // CNC, INTRADAY, MARGIN, MTF, CO, BO
    orderType = OrderType.LIMIT,          // LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
    validity = Validity.DAY,              // DAY or IOC
    securityId = "1333",
    quantity = 10,
    price = 1428.0,
    disclosedQuantity = 0,                // Optional
    triggerPrice = 0.0,                   // For SL orders
    afterMarketOrder = false,             // AMO flag
    amoTime = AmoTime.OPEN,               // PRE_OPEN, OPEN, OPEN_30, OPEN_60
    correlationId = "my-order-1"          // Optional tracking ID
))
println("Order ID: ${response.orderId}")
println("Status: ${response.orderStatus}")
```

#### Slicing Order (Large Orders)

```kotlin
// Auto-splits large orders per exchange freeze limits
val response = ordersApi.placeSlicingOrder(SlicingOrderParams(
    transactionType = TransactionType.BUY,
    exchangeSegment = ExchangeSegment.NSE_FNO,
    productType = ProductType.INTRADAY,
    orderType = OrderType.LIMIT,
    validity = Validity.DAY,
    securityId = "43225",
    quantity = 5000,  // Will be sliced into smaller orders
    price = 21000.0
))
```

#### Modify Order

```kotlin
val modified = ordersApi.modifyOrder(
    orderId = "240108010918222",
    params = ModifyOrderParams(
        orderType = OrderType.LIMIT,
        legName = LegName.ENTRY_LEG,
        quantity = 15,
        price = 1430.0,
        disclosedQuantity = 0,
        triggerPrice = 0.0,
        validity = Validity.DAY
    )
)
```

#### Cancel Order

```kotlin
val cancelled = ordersApi.cancelOrder("240108010445130")
```

#### Query Orders

```kotlin
// Get all orders for the day
val orderBook = ordersApi.getOrderBook()

// Get specific order details
val order = ordersApi.getOrderById("240108010445130")

// Get order by correlation ID
val orderByCorr = ordersApi.getOrderByCorrelationId("my-order-1")

// Get all trades for the day
val trades = ordersApi.getTradeBook()

// Get trades for specific order
val orderTrades = ordersApi.getTradesByOrderId("240108010445100")
```

### Super Orders

Multi-leg orders with entry, target, and stop-loss.

```kotlin
val superApi = dhan.getSuperOrdersApi()

// Place super order
val response = superApi.placeSuperOrder(PlaceSuperOrderParams(
    transactionType = TransactionType.BUY,
    exchangeSegment = ExchangeSegment.NSE_EQ,
    productType = ProductType.INTRADAY,
    orderType = OrderType.LIMIT,
    securityId = "1333",
    quantity = 10,
    price = 1428.0,
    targetPrice = 1450.0,
    stopLossPrice = 1410.0,
    trailingJump = 5.0  // Trailing stop-loss
))
println("Super Order ID: ${response.orderId}")

// Modify specific leg
superApi.modifySuperOrder(
    orderId = "order-id",
    params = ModifySuperOrderParams(
        legName = LegName.TARGET_LEG,
        orderType = OrderType.LIMIT,
        quantity = 10,
        price = 1460.0,
        triggerPrice = 0.0,
        validity = Validity.DAY
    )
)

// Cancel by leg
superApi.cancelSuperOrder("order-id", LegName.STOP_LOSS_LEG)

// Get all super orders
val superOrders = superApi.getSuperOrderBook()
```

### Forever Orders (GTT)

Good Till Triggered orders execute automatically when price conditions are met.

```kotlin
val foreverApi = dhan.getForeverOrdersApi()

// Place single GTT order
val response = foreverApi.placeForeverOrder(PlaceForeverOrderParams(
    orderFlag = ForeverOrderFlag.SINGLE,
    transactionType = TransactionType.BUY,
    exchangeSegment = ExchangeSegment.NSE_EQ,
    productType = ProductType.CNC,
    orderType = OrderType.LIMIT,
    validity = Validity.DAY,
    securityId = "1333",
    quantity = 10,
    price = 1400.0,
    triggerPrice = 1405.0
))
println("Forever Order ID: ${response.orderId}")

// Place OCO (One Cancels Other) order
val ocoResponse = foreverApi.placeForeverOrder(PlaceForeverOrderParams(
    orderFlag = ForeverOrderFlag.OCO,
    transactionType = TransactionType.SELL,
    exchangeSegment = ExchangeSegment.NSE_EQ,
    productType = ProductType.CNC,
    orderType = OrderType.LIMIT,
    validity = Validity.DAY,
    securityId = "1333",
    quantity = 10,
    price = 1500.0,           // Target price
    triggerPrice = 1505.0,
    price1 = 1380.0,          // Stop-loss price
    triggerPrice1 = 1385.0
))

// Modify forever order
foreverApi.modifyForeverOrder(
    orderId = "order-id",
    params = ModifyForeverOrderParams(
        orderFlag = ForeverOrderFlag.SINGLE,
        orderType = OrderType.LIMIT,
        legName = LegName.ENTRY_LEG,
        quantity = 15,
        price = 1395.0,
        triggerPrice = 1400.0,
        validity = Validity.DAY
    )
)

// Cancel forever order
foreverApi.cancelForeverOrder("order-id")

// Get all forever orders
val foreverOrders = foreverApi.getForeverOrderBook()
```

### Portfolio

#### Holdings

```kotlin
val portfolioApi = dhan.getPortfolioApi()

val holdings = portfolioApi.getHoldings()
for (holding in holdings) {
    println("${holding.tradingSymbol}: Qty=${holding.totalQuantity}, Avg=${holding.averageCostPrice}")
    println("  DP: ${holding.dpQty}, T1: ${holding.t1Qty}, Collateral: ${holding.collateralQty}")
}
```

#### Positions

```kotlin
val positions = portfolioApi.getPositions()
for (pos in positions) {
    println("${pos.tradingSymbol}: ${pos.positionType}")
    println("  Qty=${pos.netQty}, P&L=${pos.realizedProfit}")
}
```

#### Convert Position

```kotlin
val converted = portfolioApi.convertPosition(ConvertPositionParams(
    fromProductType = ProductType.INTRADAY,
    toProductType = ProductType.CNC,
    exchangeSegment = ExchangeSegment.NSE_EQ,
    positionType = PositionType.LONG,
    securityId = "1333",
    quantity = 10
))
```

### Funds & Margins

```kotlin
val fundsApi = dhan.getFundsApi()

// Get fund limit
val funds = fundsApi.getFundLimit()
println("Available Balance: ${funds.availableBalance}")
println("Utilized Amount: ${funds.utilizedAmount}")
println("Collateral Amount: ${funds.collateralAmount}")

// Calculate margin
val margin = fundsApi.calculateMargin(MarginCalculatorParams(
    exchangeSegment = ExchangeSegment.NSE_FNO,
    transactionType = TransactionType.BUY,
    quantity = 50,
    productType = ProductType.INTRADAY,
    securityId = "52175",
    price = 21000.0
))
println("Total Margin: ${margin.totalMargin}")
println("SPAN Margin: ${margin.spanMargin}")
println("Exposure Margin: ${margin.exposureMargin}")
```

### Market Quotes

Rate limit: 1 request/second, max 1000 instruments per request.

```kotlin
val quoteApi = dhan.getMarketQuoteApi()

// Get LTP for multiple instruments
// Returns nested map: exchangeSegment -> securityId -> Ltp
val ltps = quoteApi.getLtp(mapOf(
    "NSE_EQ" to listOf(1333, 11536),
    "BSE_EQ" to listOf(532540)
))
ltps.forEach { (segment, securities) ->
    securities.forEach { (securityId, ltp) ->
        println("$segment/$securityId: LTP=${ltp.lastPrice}")
    }
}

// Get OHLC data
val ohlc = quoteApi.getOhlc(mapOf(
    "NSE_EQ" to listOf(1333)
))
ohlc.forEach { (segment, securities) ->
    securities.forEach { (securityId, data) ->
        println("$segment/$securityId: O=${data.ohlc.open}, H=${data.ohlc.high}, L=${data.ohlc.low}, C=${data.ohlc.close}")
    }
}

// Get full quote with market depth
val quotes = quoteApi.getQuote(mapOf(
    "NSE_EQ" to listOf(1333)
))
quotes.forEach { (segment, securities) ->
    securities.forEach { (securityId, quote) ->
        println("$segment/$securityId: LTP=${quote.lastPrice}, Volume=${quote.volume}")
        quote.depth?.buy?.take(3)?.forEachIndexed { i, level ->
            println("  Bid $i: ${level.quantity} @ ${level.price}")
        }
    }
}
```

### Historical Data

```kotlin
val histApi = dhan.getHistoricalDataApi()

// Get daily OHLCV data
val dailyData = histApi.getDailyData(DailyHistoricalParams(
    securityId = "1333",
    exchangeSegment = ExchangeSegment.NSE_EQ,
    instrument = InstrumentType.EQUITY,
    fromDate = "2024-01-01",
    toDate = "2024-02-01"
))
dailyData.timestamp.forEachIndexed { i, ts ->
    println("$ts: O=${dailyData.open[i]}, H=${dailyData.high[i]}, L=${dailyData.low[i]}, C=${dailyData.close[i]}, V=${dailyData.volume[i]}")
}

// Get intraday data (5-minute candles)
val intradayData = histApi.getIntradayData(IntradayHistoricalParams(
    securityId = "1333",
    exchangeSegment = ExchangeSegment.NSE_EQ,
    instrument = InstrumentType.EQUITY,
    interval = ChartInterval.MINUTE_5,
    fromDate = "2024-01-15 09:15:00",
    toDate = "2024-01-15 15:30:00"
))
```

**Data availability:** Max 90 days per request, up to 5 years of historical data.

### Option Chain

Rate limit: 1 request/3 seconds.

```kotlin
val optionApi = dhan.getOptionChainApi()

// Get expiry dates
val expiries = optionApi.getExpiryList(ExpiryListParams(
    underlyingScrip = 13  // NIFTY
))
expiries.forEach { println(it) }

// Get option chain
val chain = optionApi.getOptionChain(OptionChainParams(
    underlyingScrip = 13,
    expiry = expiries.first()
))
println("Underlying LTP: ${chain.lastPrice}")
chain.optionChain.forEach { (strike, data) ->
    println("Strike $strike:")
    data.callOption?.let { println("  CE: LTP=${it.lastPrice}, IV=${it.impliedVolatility}") }
    data.putOption?.let { println("  PE: LTP=${it.lastPrice}, IV=${it.impliedVolatility}") }
}
```

### Instruments

Download instrument master data. **No authentication required.**

```kotlin
val instrumentsApi = dhan.getInstrumentsApi()

// Get compact CSV URL (essential instrument data)
val compactUrl = instrumentsApi.getCompactCsvUrl()
// https://images.dhan.co/api-data/api-scrip-master.csv

// Get detailed CSV URL (includes margin requirements)
val detailedUrl = instrumentsApi.getDetailedCsvUrl()
// https://images.dhan.co/api-data/api-scrip-master-detailed.csv

// Get instruments for specific segment (returns raw CSV/JSON)
val nseEquity = instrumentsApi.getInstruments(ExchangeSegment.NSE_EQ)
val nseFno = instrumentsApi.getInstruments(ExchangeSegment.NSE_FNO)
```

### EDIS

Electronic Delivery Instruction Slip for authorizing delivery sell orders.

```kotlin
val edisApi = dhan.getEdisApi()

// Step 1: Generate T-PIN (sent to registered mobile)
edisApi.generateTpin()

// Step 2: Generate EDIS form
val form = edisApi.generateEdisForm(EdisFormParams(
    isin = "INE733E01010",
    quantity = 10,
    exchange = Exchange.NSE
))
// Display form.edisFormHtml to user for T-PIN entry

// Step 3: Check status
val status = edisApi.inquireEdisStatus("INE733E01010")
// Use "ALL" to check all holdings
println("Approved: ${status.approvedQuantity}/${status.totalQuantity}")
```

### Traders Control

```kotlin
val tradersControlApi = dhan.getTradersControlApi()

// Activate kill switch (disable all trading for the day)
tradersControlApi.setKillSwitch(KillSwitchStatus.ACTIVATE)

// Deactivate kill switch (re-enable trading)
tradersControlApi.setKillSwitch(KillSwitchStatus.DEACTIVATE)
```

### Statement

```kotlin
val statementApi = dhan.getStatementApi()

// Get ledger entries
val ledger = statementApi.getLedger(
    fromDate = "2024-01-01",
    toDate = "2024-01-31"
)

// Get trade history (paginated)
val trades = statementApi.getTradeHistory(
    fromDate = "2024-01-01",
    toDate = "2024-01-31",
    page = 0
)
```

---

## Error Handling

```kotlin
import io.github.sonicalgo.dhan.exception.DhanApiException

try {
    val order = dhan.getOrdersApi().placeOrder(params)
} catch (e: DhanApiException) {
    println("HTTP Status: ${e.httpStatusCode}")
    println("Message: ${e.message}")

    // Helper methods
    when {
        e.isRateLimitError -> println("Rate limited (429) - slow down requests")
        e.isAuthenticationError -> println("Auth failed (401/403) - check token")
        e.isValidationError -> println("Bad request (400) - check parameters")
        e.isServerError -> println("Server error (5xx) - retry later")
        e.isNetworkError -> println("Network error - check connection")
    }
}
```

### Exception Types

| Exception | Description |
|-----------|-------------|
| `DhanApiException` | Exception for all HTTP API errors with helper methods |

---

## Resource Management

The SDK implements `Closeable` for unified resource cleanup. A single `close()` call handles everything:

```kotlin
// Create SDK and WebSocket clients
val dhan = Dhan.builder().clientId("id").accessToken("token").build()
val feedClient = dhan.createMarketFeedClient()
val orderClient = dhan.createOrderUpdateClient()

// ... use SDK ...

// Single close handles all cleanup (WebSocket clients + HTTP resources)
dhan.close()
```

Or use try-with-resources for automatic cleanup:

```kotlin
// Kotlin
Dhan.builder().clientId("id").accessToken("token").build().use { dhan ->
    val feedClient = dhan.createMarketFeedClient()
    feedClient.connect()
    // ... use SDK ...
} // Auto-closes when block exits
```

<details>
<summary>Java</summary>

```java
// Java try-with-resources
try (Dhan dhan = Dhan.builder().clientId("id").accessToken("token").build()) {
    var feedClient = dhan.createMarketFeedClient();
    feedClient.connect();
    // ... use SDK ...
} // Auto-closes when block exits
```

</details>

---

## Thread Safety

- All API modules are thread-safe and can be called from any thread
- WebSocket callbacks are invoked on background threads
- Use appropriate synchronization when updating UI from callbacks

---

## Requirements

- **Java 11** or higher
- **Kotlin 2.2** or higher (if using Kotlin)
- Dhan trading account with API access

### Dependencies

- trading-core 1.0.0
- OkHttp 5.3.0
- Jackson 2.20.1

---

## License

This project is licensed under the MIT License.

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## Links

- [DhanHQ API Documentation](https://dhanhq.co/docs/v2/)
- [Report Issues](https://github.com/SonicAlgo/dhan-java-sdk/issues)

---

## Disclaimer

This is an **unofficial** SDK and is not affiliated with, endorsed by, or supported by DhanHQ. Use at your own risk. Always verify API behavior against the [official DhanHQ documentation](https://dhanhq.co/docs/v2/). Trading involves financial risk.
