# Dhan Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.sonicalgo/dhan-java-sdk)](https://central.sonatype.com/artifact/io.github.sonicalgo/dhan-java-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)

Unofficial Kotlin/Java SDK for the [Dhan](https://dhanhq.co) trading platform. Supports REST APIs and real-time WebSocket streaming.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.sonicalgo:dhan-java-sdk:2.2.0")
```

### Gradle (Groovy)

```groovy
implementation 'io.github.sonicalgo:dhan-java-sdk:2.2.0'
```

### Maven

```xml
<dependency>
    <groupId>io.github.sonicalgo</groupId>
    <artifactId>dhan-java-sdk</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Quick Start

<details open>
<summary>Kotlin</summary>

```kotlin
import io.github.sonicalgo.dhan.Dhan
import io.github.sonicalgo.dhan.usecase.*
import io.github.sonicalgo.dhan.common.*

// Create SDK instance
val dhan = Dhan.builder()
    .clientId("your-client-id")
    .accessToken("your-access-token")
    .build()

// Get user profile
val profile = dhan.getProfile()
println("Client: ${profile.dhanClientId}")

// Get market quote
val ltps = dhan.getLtp(mapOf(
    "NSE_EQ" to listOf(1333)  // HDFC Bank
))
println("LTP: ${ltps["NSE_EQ"]?.get("1333")?.lastPrice}")

// Place an order
val response = dhan.placeOrder(PlaceOrderParams {
    transactionType = TransactionType.BUY
    exchangeSegment = ExchangeSegment.NSE_EQ
    productType = ProductType.CNC
    orderType = OrderType.LIMIT
    validity = Validity.DAY
    securityId = "1333"
    quantity = 10
    price = 1428.0
})
println("Order ID: ${response.orderId}")
```

</details>

<details>
<summary>Java</summary>

```java
import io.github.sonicalgo.dhan.Dhan;
import io.github.sonicalgo.dhan.usecase.*;
import io.github.sonicalgo.dhan.common.*;
import java.util.Map;
import java.util.List;

// Create SDK instance
Dhan dhan = Dhan.builder()
    .clientId("your-client-id")
    .accessToken("your-access-token")
    .build();

// Get user profile
var profile = dhan.getProfile();
System.out.println("Client: " + profile.getDhanClientId());

// Get market quote
var ltps = dhan.getLtp(Map.of(
    "NSE_EQ", List.of(1333)  // HDFC Bank
));
System.out.println("LTP: " + ltps.get("NSE_EQ").get("1333").getLastPrice());

// Place an order using builder
var response = dhan.placeOrder(PlaceOrderParamsBuilder.builder()
    .transactionType(TransactionType.BUY)
    .exchangeSegment(ExchangeSegment.NSE_EQ)
    .productType(ProductType.CNC)
    .orderType(OrderType.LIMIT)
    .validity(Validity.DAY)
    .securityId("1333")
    .quantity(10)
    .price(1428.0)
    .build());
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
val order = dhan.getOrderById("order-id")
when (order.orderStatus) {
    OrderStatus.TRADED -> println("Order filled")
    OrderStatus.REJECTED -> println("Order rejected")
    OrderStatus.PENDING -> println("Order pending")
    OrderStatus.PART_TRADED -> println("Partial fill: ${order.filledQty}/${order.quantity}")
    else -> println("Status: ${order.orderStatus}")
}

// Position uses enums
val positions = dhan.getPositions()
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

- **43 REST API operations** - Orders, Portfolio, Market Quotes, Historical Data, Option Chain, and more
- **Real-time market data** - WebSocket streaming with binary protocol (low latency)
- **Real-time order updates** - Order and trade updates via WebSocket
- **Automatic reconnection** - WebSocket clients reconnect with exponential backoff
- **Configurable rate limiting** - Automatic retry with exponential backoff for HTTP 429
- **Debug logging** - Optional HTTP request/response logging for troubleshooting
- **Full Kotlin & Java compatibility** - Use from either language

---

## API Reference

| Method | Description |
|--------|-------------|
| **Authentication** | |
| `generateConsent(appId, appSecret)` | Generate consent app ID for auth flow |
| `consumeConsent(tokenId, appId, appSecret)` | Exchange token for access credentials |
| `getProfile()` | Get user profile |
| `setIp()` | Set static IP for order APIs |
| `modifyIp()` | Modify IP (once per 7 days) |
| `getIpConfiguration()` | Get current IP config |
| `renewToken()` | Extend token validity |
| **Orders** | |
| `placeOrder()` | Place a single order |
| `placeSlicingOrder()` | Place slicing order (auto-split) |
| `modifyOrder()` | Modify an existing order |
| `cancelOrder()` | Cancel an order |
| `getOrders()` | Get all orders for the day |
| `getOrderById()` | Get specific order |
| `getOrderByCorrelationId()` | Get order by correlation ID |
| `getTrades()` | Get all trades for the day |
| `getTradesByOrderId()` | Get trades for specific order |
| **Super Orders** | |
| `placeSuperOrder()` | Place super order (entry + target + SL) |
| `modifySuperOrder()` | Modify super order leg |
| `cancelSuperOrder()` | Cancel super order leg |
| `getSuperOrders()` | Get all super orders |
| **Forever Orders (GTT)** | |
| `placeForeverOrder()` | Place GTT order |
| `modifyForeverOrder()` | Modify GTT order |
| `cancelForeverOrder()` | Cancel GTT order |
| `getForeverOrders()` | Get all GTT orders |
| **Portfolio** | |
| `getHoldings()` | Get holdings |
| `getPositions()` | Get positions |
| `convertPosition()` | Convert position product type |
| **Funds** | |
| `getFundLimits()` | Get fund limits |
| `calculateMargin()` | Calculate margin requirements |
| **Market Quotes** | |
| `getLtp()` | Get last traded price |
| `getOhlc()` | Get OHLC data |
| `getQuote()` | Get full quote with depth |
| **Historical Data** | |
| `getDailyHistory()` | Get daily OHLCV |
| `getIntradayHistory()` | Get intraday OHLCV |
| **Option Chain** | |
| `getExpiryList()` | Get expiry dates |
| `getOptionChain()` | Get option chain |
| **Instruments** | |
| `getCompactCsvUrl()` | Get compact CSV URL |
| `getDetailedCsvUrl()` | Get detailed CSV URL |
| `getInstruments()` | Download segment instruments |
| **EDIS** | |
| `generateTpin()` | Generate T-PIN |
| `generateEdisForm()` | Generate EDIS form |
| `inquireEdisStatus()` | Check EDIS status |
| **Traders Control** | |
| `setKillSwitch()` | Activate/deactivate kill switch |
| **Statement** | |
| `getLedger()` | Get ledger entries |
| `getTradeHistory()` | Get trade history |
| **WebSocket** | |
| `createMarketFeedClient()` | Create market data WebSocket |
| `createOrderStreamClient()` | Create order update WebSocket |

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
val orderClient = dhan.createOrderStreamClient(
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

- [API Reference](#api-reference)
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
import io.github.sonicalgo.dhan.websocket.marketFeed.*

// Create client with custom reconnection settings (optional)
val feedClient = dhan.createMarketFeedClient(
    maxReconnectAttempts = 10,      // Default: 5
    autoReconnectEnabled = true,    // Default: true
    autoResubscribeEnabled = true   // Default: true, auto-resubscribe after reconnect
)

// Add listener
feedClient.addListener(object : MarketFeedListener {
    override fun onConnected() {
        println("Connected to market feed!")
        // Subscribe with TICKER mode
        feedClient.subscribe(
            listOf(
                Instrument(ExchangeSegment.NSE_EQ, "1333"),   // HDFC Bank
                Instrument(ExchangeSegment.NSE_EQ, "11536"),  // TCS
                Instrument(ExchangeSegment.IDX_I, "26000")    // Nifty 50
            ),
            FeedMode.TICKER
        )
    }

    override fun onReconnected() {
        println("Reconnected! Subscriptions restored.")
    }

    override fun onDisconnected(code: Int, reason: String) {
        println("Disconnected: $reason")
    }

    override fun onError(error: Throwable) {
        println("Error: ${error.message}")
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
feedClient.subscribe(listOf(Instrument(ExchangeSegment.NSE_EQ, "1333")), FeedMode.TICKER)

// Change mode for specific instruments
feedClient.subscribe(listOf(Instrument(ExchangeSegment.NSE_EQ, "1333")), FeedMode.QUOTE)

// Subscribe to full mode with market depth
feedClient.subscribe(listOf(Instrument(ExchangeSegment.NSE_FNO, "43225")), FeedMode.FULL)

// Unsubscribe
feedClient.unsubscribe(listOf(Instrument(ExchangeSegment.NSE_EQ, "11536")))

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
Instrument(ExchangeSegment.NSE_EQ, "1333")       // NSE stocks
Instrument(ExchangeSegment.NSE_FNO, "43225")     // NSE F&O
Instrument(ExchangeSegment.IDX_I, "26000")       // NIFTY 50
Instrument(ExchangeSegment.MCX_COMM, "224035")   // MCX instruments
Instrument(ExchangeSegment.BSE_EQ, "532540")     // BSE stocks
Instrument(ExchangeSegment.BSE_FNO, "...")       // BSE F&O
Instrument(ExchangeSegment.NSE_CURRENCY, "...")  // NSE Currency
Instrument(ExchangeSegment.BSE_CURRENCY, "...")  // BSE Currency
```

### Order Updates

Real-time order and trade updates via WebSocket.

```kotlin
import io.github.sonicalgo.dhan.websocket.order.*

// Create client with custom reconnection settings (optional)
val orderClient = dhan.createOrderStreamClient(
    maxReconnectAttempts = 10,   // Default: 5
    autoReconnectEnabled = true  // Default: true
)

// Add listener
orderClient.addListener(object : OrderStreamListener {
    override fun onConnected() {
        println("Connected to order updates!")
    }

    override fun onReconnected() {
        println("Reconnected to order updates")
    }

    override fun onDisconnected(code: Int, reason: String) {
        println("Disconnected: $reason")
    }

    override fun onError(error: Throwable) {
        println("Error: ${error.message}")
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

    override fun onReconnecting(attempt: Int, delayMs: Long) {
        println("Reconnecting (attempt $attempt) in ${delayMs}ms...")
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
dhan.getOrders()  // Uses new token

// WebSocket needs reconnection to use new token
feedClient.close()
feedClient.connect()  // Now authenticates with new token
```

---

## REST API Reference

### Authentication

#### Consent-Based Authentication Flow

For individual traders using the API Key method, the SDK provides a consent-based authentication flow:

<details open>
<summary>Kotlin</summary>

```kotlin
// Step 1: Create SDK instance with just client ID
val dhan = Dhan.builder()
    .clientId("1000000001")
    .build()

// Step 2: Generate consent (returns consentAppId)
val consent = dhan.generateConsent(
    appId = "your-app-id",
    appSecret = "your-app-secret"
)
println("Consent App ID: ${consent.consentAppId}")

// Step 3: Redirect user to browser for login
// URL: https://login.dhan.co?consent_id=${consent.consentAppId}
// After login, user is redirected to your callback URL with tokenId

// Step 4: Exchange token for access credentials
val credentials = dhan.consumeConsent(
    tokenId = "token-from-callback",
    appId = "your-app-id",
    appSecret = "your-app-secret"
)
println("Access Token: ${credentials.accessToken}")
println("Client Name: ${credentials.dhanClientName}")
println("Expires: ${credentials.expiryTime}")

// Step 5: Set access token for trading APIs
dhan.setAccessToken(credentials.accessToken)

// Now you can use trading APIs
val profile = dhan.getProfile()
```

</details>

<details>
<summary>Java</summary>

```java
// Step 1: Create SDK instance with just client ID
Dhan dhan = Dhan.builder()
    .clientId("1000000001")
    .build();

// Step 2: Generate consent (returns consentAppId)
GenerateConsentResult consent = dhan.generateConsent("your-app-id", "your-app-secret");
System.out.println("Consent App ID: " + consent.getConsentAppId());

// Step 3: Redirect user to browser for login
// URL: https://login.dhan.co?consent_id=<consentAppId>
// After login, user is redirected to your callback URL with tokenId

// Step 4: Exchange token for access credentials
ConsumeConsentResult credentials = dhan.consumeConsent(
    "token-from-callback",
    "your-app-id",
    "your-app-secret"
);
System.out.println("Access Token: " + credentials.getAccessToken());
System.out.println("Client Name: " + credentials.getDhanClientName());

// Step 5: Set access token for trading APIs
dhan.setAccessToken(credentials.getAccessToken());

// Now you can use trading APIs
var profile = dhan.getProfile();
```

</details>

> **Note:** Users can generate up to 25 consent app IDs daily.

#### Profile and IP Configuration

```kotlin
// Get user profile
val profile = dhan.getProfile()
// Returns: dhanClientId, tokenValidity, activeSegments, dataPlan

// Set static IP (required for order APIs)
dhan.setIp(SetIpParams {
    ip = "203.0.113.50"
    ipFlag = IpFlag.PRIMARY
})

// Modify IP (allowed once every 7 days)
dhan.modifyIp(SetIpParams {
    ip = "203.0.113.51"
    ipFlag = IpFlag.SECONDARY
})

// Get IP configuration
val ipConfig = dhan.getIpConfiguration()
println("Primary IP: ${ipConfig.primaryIp}")
println("Secondary IP: ${ipConfig.secondaryIp}")

// Renew token (extends validity for 24 hours)
dhan.renewToken()
```

### Orders

> **Note:** Order placement, modification, and cancellation APIs require static IP whitelisting. Configure your IP at [web.dhan.co](https://web.dhan.co).

#### Place Order

<details open>
<summary>Kotlin</summary>

```kotlin
val response = dhan.placeOrder(PlaceOrderParams {
    transactionType = TransactionType.BUY
    exchangeSegment = ExchangeSegment.NSE_EQ
    productType = ProductType.CNC        // CNC, INTRADAY, MARGIN, MTF, CO, BO
    orderType = OrderType.LIMIT          // LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
    validity = Validity.DAY              // DAY or IOC
    securityId = "1333"
    quantity = 10
    price = 1428.0
    disclosedQuantity = 0                // Optional
    triggerPrice = 0.0                   // For SL orders
    afterMarketOrder = false             // AMO flag
    amoTime = AmoTime.OPEN               // PRE_OPEN, OPEN, OPEN_30, OPEN_60
    correlationId = "my-order-1"         // Optional tracking ID
})
println("Order ID: ${response.orderId}")
println("Status: ${response.orderStatus}")
```

</details>

<details>
<summary>Java</summary>

```java
var response = dhan.placeOrder(PlaceOrderParamsBuilder.builder()
    .transactionType(TransactionType.BUY)
    .exchangeSegment(ExchangeSegment.NSE_EQ)
    .productType(ProductType.CNC)
    .orderType(OrderType.LIMIT)
    .validity(Validity.DAY)
    .securityId("1333")
    .quantity(10)
    .price(1428.0)
    .disclosedQuantity(0)
    .triggerPrice(0.0)
    .afterMarketOrder(false)
    .amoTime(AmoTime.OPEN)
    .correlationId("my-order-1")
    .build());
System.out.println("Order ID: " + response.getOrderId());
System.out.println("Status: " + response.getOrderStatus());
```

</details>

#### Slicing Order (Large Orders)

```kotlin
// Auto-splits large orders per exchange freeze limits
val response = dhan.placeSlicingOrder(SlicingOrderParams {
    transactionType = TransactionType.BUY
    exchangeSegment = ExchangeSegment.NSE_FNO
    productType = ProductType.INTRADAY
    orderType = OrderType.LIMIT
    validity = Validity.DAY
    securityId = "43225"
    quantity = 5000  // Will be sliced into smaller orders
    price = 21000.0
})
```

#### Modify Order

<details open>
<summary>Kotlin</summary>

```kotlin
val modified = dhan.modifyOrder(
    orderId = "240108010918222",
    params = ModifyOrderParams {
        orderType = OrderType.LIMIT
        legName = LegName.ENTRY_LEG
        quantity = 15
        price = 1430.0
        disclosedQuantity = 0
        triggerPrice = 0.0
        validity = Validity.DAY
    }
)
```

</details>

<details>
<summary>Java</summary>

```java
var modified = dhan.modifyOrder(
    "240108010918222",
    ModifyOrderParamsBuilder.builder()
        .orderType(OrderType.LIMIT)
        .legName(LegName.ENTRY_LEG)
        .quantity(15)
        .price(1430.0)
        .disclosedQuantity(0)
        .triggerPrice(0.0)
        .validity(Validity.DAY)
        .build()
);
```

</details>

#### Cancel Order

```kotlin
val cancelled = dhan.cancelOrder("240108010445130")
```

#### Query Orders

```kotlin
// Get all orders for the day
val orderBook = dhan.getOrders()

// Get specific order details
val order = dhan.getOrderById("240108010445130")

// Get order by correlation ID
val orderByCorr = dhan.getOrderByCorrelationId("my-order-1")

// Get all trades for the day
val trades = dhan.getTrades()

// Get trades for specific order
val orderTrades = dhan.getTradesByOrderId("240108010445100")
```

### Super Orders

Multi-leg orders with entry, target, and stop-loss.

<details open>
<summary>Kotlin</summary>

```kotlin
// Place super order
val response = dhan.placeSuperOrder(PlaceSuperOrderParams {
    transactionType = TransactionType.BUY
    exchangeSegment = ExchangeSegment.NSE_EQ
    productType = ProductType.INTRADAY
    orderType = OrderType.LIMIT
    securityId = "1333"
    quantity = 10
    price = 1428.0
    targetPrice = 1450.0
    stopLossPrice = 1410.0
    trailingJump = 5.0  // Trailing stop-loss
})
println("Super Order ID: ${response.orderId}")

// Modify specific leg
dhan.modifySuperOrder(
    orderId = "order-id",
    params = ModifySuperOrderParams {
        legName = LegName.TARGET_LEG
        orderType = OrderType.LIMIT
        quantity = 10
        price = 1460.0
        triggerPrice = 0.0
        validity = Validity.DAY
    }
)

// Cancel by leg
dhan.cancelSuperOrder("order-id", LegName.STOP_LOSS_LEG)

// Get all super orders
val superOrders = dhan.getSuperOrders()
```

</details>

<details>
<summary>Java</summary>

```java
// Place super order
var response = dhan.placeSuperOrder(PlaceSuperOrderParamsBuilder.builder()
    .transactionType(TransactionType.BUY)
    .exchangeSegment(ExchangeSegment.NSE_EQ)
    .productType(ProductType.INTRADAY)
    .orderType(OrderType.LIMIT)
    .securityId("1333")
    .quantity(10)
    .price(1428.0)
    .targetPrice(1450.0)
    .stopLossPrice(1410.0)
    .trailingJump(5.0)
    .build());
System.out.println("Super Order ID: " + response.getOrderId());

// Modify specific leg
dhan.modifySuperOrder(
    "order-id",
    ModifySuperOrderParamsBuilder.builder()
        .legName(LegName.TARGET_LEG)
        .orderType(OrderType.LIMIT)
        .quantity(10)
        .price(1460.0)
        .triggerPrice(0.0)
        .validity(Validity.DAY)
        .build()
);

// Cancel by leg
dhan.cancelSuperOrder("order-id", LegName.STOP_LOSS_LEG);

// Get all super orders
var superOrders = dhan.getSuperOrders();
```

</details>

### Forever Orders (GTT)

Good Till Triggered orders execute automatically when price conditions are met.

<details open>
<summary>Kotlin</summary>

```kotlin
// Place single GTT order
val response = dhan.placeForeverOrder(PlaceForeverOrderParams {
    orderFlag = ForeverOrderFlag.SINGLE
    transactionType = TransactionType.BUY
    exchangeSegment = ExchangeSegment.NSE_EQ
    productType = ProductType.CNC
    orderType = OrderType.LIMIT
    validity = Validity.DAY
    securityId = "1333"
    quantity = 10
    price = 1400.0
    triggerPrice = 1405.0
})
println("Forever Order ID: ${response.orderId}")

// Place OCO (One Cancels Other) order
val ocoResponse = dhan.placeForeverOrder(PlaceForeverOrderParams {
    orderFlag = ForeverOrderFlag.OCO
    transactionType = TransactionType.SELL
    exchangeSegment = ExchangeSegment.NSE_EQ
    productType = ProductType.CNC
    orderType = OrderType.LIMIT
    validity = Validity.DAY
    securityId = "1333"
    quantity = 10
    price = 1500.0           // Target price
    triggerPrice = 1505.0
    price1 = 1380.0          // Stop-loss price
    triggerPrice1 = 1385.0
})

// Modify forever order
dhan.modifyForeverOrder(
    orderId = "order-id",
    params = ModifyForeverOrderParams {
        orderFlag = ForeverOrderFlag.SINGLE
        orderType = OrderType.LIMIT
        legName = LegName.ENTRY_LEG
        quantity = 15
        price = 1395.0
        triggerPrice = 1400.0
        validity = Validity.DAY
    }
)

// Cancel forever order
dhan.cancelForeverOrder("order-id")

// Get all forever orders
val foreverOrders = dhan.getForeverOrders()
```

</details>

<details>
<summary>Java</summary>

```java
// Place single GTT order
var response = dhan.placeForeverOrder(PlaceForeverOrderParamsBuilder.builder()
    .orderFlag(ForeverOrderFlag.SINGLE)
    .transactionType(TransactionType.BUY)
    .exchangeSegment(ExchangeSegment.NSE_EQ)
    .productType(ProductType.CNC)
    .orderType(OrderType.LIMIT)
    .validity(Validity.DAY)
    .securityId("1333")
    .quantity(10)
    .price(1400.0)
    .triggerPrice(1405.0)
    .build());
System.out.println("Forever Order ID: " + response.getOrderId());

// Modify forever order
dhan.modifyForeverOrder(
    "order-id",
    ModifyForeverOrderParamsBuilder.builder()
        .orderFlag(ForeverOrderFlag.SINGLE)
        .orderType(OrderType.LIMIT)
        .legName(LegName.ENTRY_LEG)
        .quantity(15)
        .price(1395.0)
        .triggerPrice(1400.0)
        .validity(Validity.DAY)
        .build()
);

// Cancel forever order
dhan.cancelForeverOrder("order-id");

// Get all forever orders
var foreverOrders = dhan.getForeverOrders();
```

</details>

### Portfolio

#### Holdings

```kotlin
val holdings = dhan.getHoldings()
for (holding in holdings) {
    println("${holding.tradingSymbol}: Qty=${holding.totalQuantity}, Avg=${holding.averageCostPrice}")
    println("  DP: ${holding.dpQty}, T1: ${holding.t1Qty}, Collateral: ${holding.collateralQty}")
}
```

#### Positions

```kotlin
val positions = dhan.getPositions()
for (pos in positions) {
    println("${pos.tradingSymbol}: ${pos.positionType}")
    println("  Qty=${pos.netQty}, P&L=${pos.realizedProfit}")
}
```

#### Convert Position

<details open>
<summary>Kotlin</summary>

```kotlin
val converted = dhan.convertPosition(ConvertPositionParams {
    fromProductType = ProductType.INTRADAY
    toProductType = ProductType.CNC
    exchangeSegment = ExchangeSegment.NSE_EQ
    positionType = PositionType.LONG
    securityId = "1333"
    quantity = 10
})
```

</details>

<details>
<summary>Java</summary>

```java
var converted = dhan.convertPosition(ConvertPositionParamsBuilder.builder()
    .fromProductType(ProductType.INTRADAY)
    .toProductType(ProductType.CNC)
    .exchangeSegment(ExchangeSegment.NSE_EQ)
    .positionType(PositionType.LONG)
    .securityId("1333")
    .quantity(10)
    .build());
```

</details>

### Funds & Margins

<details open>
<summary>Kotlin</summary>

```kotlin
// Get fund limit
val funds = dhan.getFundLimits()
println("Available Balance: ${funds.availableBalance}")
println("Utilized Amount: ${funds.utilizedAmount}")
println("Collateral Amount: ${funds.collateralAmount}")

// Calculate margin
val margin = dhan.calculateMargin(CalculateMarginParams {
    exchangeSegment = ExchangeSegment.NSE_FNO
    transactionType = TransactionType.BUY
    quantity = 50
    productType = ProductType.INTRADAY
    securityId = "52175"
    price = 21000.0
})
println("Total Margin: ${margin.totalMargin}")
println("SPAN Margin: ${margin.spanMargin}")
println("Exposure Margin: ${margin.exposureMargin}")
```

</details>

<details>
<summary>Java</summary>

```java
// Get fund limit
var funds = dhan.getFundLimits();
System.out.println("Available Balance: " + funds.getAvailableBalance());
System.out.println("Utilized Amount: " + funds.getUtilizedAmount());

// Calculate margin
var margin = dhan.calculateMargin(CalculateMarginParamsBuilder.builder()
    .exchangeSegment(ExchangeSegment.NSE_FNO)
    .transactionType(TransactionType.BUY)
    .quantity(50)
    .productType(ProductType.INTRADAY)
    .securityId("52175")
    .price(21000.0)
    .build());
System.out.println("Total Margin: " + margin.getTotalMargin());
System.out.println("SPAN Margin: " + margin.getSpanMargin());
```

</details>

### Market Quotes

Rate limit: 1 request/second, max 1000 instruments per request.

```kotlin
// Get LTP for multiple instruments
// Returns nested map: exchangeSegment -> securityId -> Ltp
val ltps = dhan.getLtp(mapOf(
    "NSE_EQ" to listOf(1333, 11536),
    "BSE_EQ" to listOf(532540)
))
ltps.forEach { (segment, securities) ->
    securities.forEach { (securityId, ltp) ->
        println("$segment/$securityId: LTP=${ltp.lastPrice}")
    }
}

// Get OHLC data
val ohlc = dhan.getOhlc(mapOf(
    "NSE_EQ" to listOf(1333)
))
ohlc.forEach { (segment, securities) ->
    securities.forEach { (securityId, data) ->
        println("$segment/$securityId: O=${data.ohlc.open}, H=${data.ohlc.high}, L=${data.ohlc.low}, C=${data.ohlc.close}")
    }
}

// Get full quote with market depth
val quotes = dhan.getQuote(mapOf(
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

<details open>
<summary>Kotlin</summary>

```kotlin
// Get daily OHLCV data
val dailyData = dhan.getDailyHistory(DailyHistoryParams {
    securityId = "1333"
    exchangeSegment = ExchangeSegment.NSE_EQ
    instrument = InstrumentType.EQUITY
    fromDate = "2024-01-01"
    toDate = "2024-02-01"
})
dailyData.timestamp.forEachIndexed { i, ts ->
    println("$ts: O=${dailyData.open[i]}, H=${dailyData.high[i]}, L=${dailyData.low[i]}, C=${dailyData.close[i]}, V=${dailyData.volume[i]}")
}

// Get intraday data (5-minute candles)
val intradayData = dhan.getIntradayHistory(IntradayHistoryParams {
    securityId = "1333"
    exchangeSegment = ExchangeSegment.NSE_EQ
    instrument = InstrumentType.EQUITY
    interval = ChartInterval.MINUTE_5
    fromDate = "2024-01-15 09:15:00"
    toDate = "2024-01-15 15:30:00"
})
```

</details>

<details>
<summary>Java</summary>

```java
// Get daily OHLCV data
var dailyData = dhan.getDailyHistory(DailyHistoryParamsBuilder.builder()
    .securityId("1333")
    .exchangeSegment(ExchangeSegment.NSE_EQ)
    .instrument(InstrumentType.EQUITY)
    .fromDate("2024-01-01")
    .toDate("2024-02-01")
    .build());

// Get intraday data (5-minute candles)
var intradayData = dhan.getIntradayHistory(IntradayHistoryParamsBuilder.builder()
    .securityId("1333")
    .exchangeSegment(ExchangeSegment.NSE_EQ)
    .instrument(InstrumentType.EQUITY)
    .interval(ChartInterval.MINUTE_5)
    .fromDate("2024-01-15 09:15:00")
    .toDate("2024-01-15 15:30:00")
    .build());
```

</details>

**Data availability:** Max 90 days per request, up to 5 years of historical data.

### Option Chain

Rate limit: 1 request/3 seconds.

```kotlin
// Get expiry dates
val expiries = dhan.getExpiryList(ExpiryListParams {
    underlyingScrip = 13  // NIFTY
})
expiries.forEach { println(it) }

// Get option chain
val chain = dhan.getOptionChain(OptionChainParams {
    underlyingScrip = 13
    expiry = expiries.first()
})
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
// Get compact CSV URL (essential instrument data)
val compactUrl = dhan.getCompactCsvUrl()
// https://images.dhan.co/api-data/api-scrip-master.csv

// Get detailed CSV URL (includes margin requirements)
val detailedUrl = dhan.getDetailedCsvUrl()
// https://images.dhan.co/api-data/api-scrip-master-detailed.csv

// Get instruments for specific segment (returns raw CSV/JSON)
val nseEquity = dhan.getInstruments(ExchangeSegment.NSE_EQ)
val nseFno = dhan.getInstruments(ExchangeSegment.NSE_FNO)
```

### EDIS

Electronic Delivery Instruction Slip for authorizing delivery sell orders.

```kotlin
// Step 1: Generate T-PIN (sent to registered mobile)
dhan.generateTpin()

// Step 2: Generate EDIS form
val form = dhan.generateEdisForm(GenerateEdisFormParams {
    isin = "INE733E01010"
    quantity = 10
    exchange = Exchange.NSE
})
// Display form.edisFormHtml to user for T-PIN entry

// Step 3: Check status
val status = dhan.inquireEdisStatus("INE733E01010")
// Use "ALL" to check all holdings
println("Approved: ${status.approvedQuantity}/${status.totalQuantity}")
```

### Traders Control

```kotlin
// Activate kill switch (disable all trading for the day)
dhan.setKillSwitch(KillSwitchStatus.ACTIVATE)

// Deactivate kill switch (re-enable trading)
dhan.setKillSwitch(KillSwitchStatus.DEACTIVATE)
```

### Statement

```kotlin
// Get ledger entries
val ledger = dhan.getLedger(GetLedgerParams {
    fromDate = "2024-01-01"
    toDate = "2024-01-31"
})

// Get trade history (paginated)
val trades = dhan.getTradeHistory(GetTradeHistoryParams {
    fromDate = "2024-01-01"
    toDate = "2024-01-31"
    page = 0
})
```

---

## Error Handling

```kotlin
import io.github.sonicalgo.dhan.exception.DhanApiException

try {
    val order = dhan.placeOrder(params)
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
val orderClient = dhan.createOrderStreamClient()

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

- All API methods are thread-safe and can be called from any thread
- WebSocket callbacks are invoked on background threads
- Use appropriate synchronization when updating UI from callbacks

---

## Requirements

- **Java 11** or higher
- **Kotlin 2.2** or higher (if using Kotlin)
- Dhan trading account with API access

### Dependencies

- trading-core 1.2.0
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
