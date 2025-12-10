package io.github.sonicalgo.dhan.model.ws

import io.github.sonicalgo.dhan.model.enums.*

/**
 * Real-time order update data from WebSocket.
 *
 * Represents a single order update event pushed by the server
 * when an order status changes.
 *
 * ## Example
 *
 * ```kotlin
 * override fun onOrderUpdate(update: OrderUpdate) {
 *     println("Order ${update.orderId}: ${update.orderStatus}")
 *     println("  ${update.tradingSymbol}: ${update.transactionType}")
 *     println("  Qty: ${update.filledQty}/${update.quantity}")
 *
 *     if (update.isFinalState) {
 *         println("Order completed!")
 *     }
 *
 *     if (update.hasError) {
 *         println("Error: ${update.omsErrorDescription}")
 *     }
 * }
 * ```
 *
 * @property dhanClientId User's Dhan client ID
 * @property orderId Dhan order reference number
 * @property correlationId User-defined tracking identifier
 * @property orderStatus Current order status
 * @property transactionType BUY or SELL
 * @property exchangeSegment Exchange and segment
 * @property productType Product type (CNC, INTRADAY, etc.)
 * @property orderType Order type (LIMIT, MARKET, etc.)
 * @property validity Order validity (DAY, IOC, etc.)
 * @property tradingSymbol Exchange trading symbol
 * @property securityId Dhan security identifier
 * @property quantity Total order quantity
 * @property disclosedQuantity Disclosed quantity
 * @property price Limit price (0 for market orders)
 * @property triggerPrice Stop-loss trigger price
 * @property afterMarketOrder After-market order flag
 * @property boProfitValue BO profit target value
 * @property boStopLossValue BO stop-loss value
 * @property legName Leg identifier for multi-leg orders
 * @property createTime Order creation timestamp
 * @property updateTime Last update timestamp
 * @property exchangeTime Exchange acknowledgment time
 * @property drvExpiryDate Derivative expiry date
 * @property drvOptionType Call or Put for options
 * @property drvStrikePrice Strike price for options
 * @property omsErrorCode Exchange error code
 * @property omsErrorDescription Exchange error message
 * @property filledQty Quantity filled so far
 * @property pendingQty Quantity pending
 * @property cancelledQty Quantity cancelled
 * @property tradedPrice Weighted average traded price
 * @property instrument Instrument type identifier
 * @property isin ISIN of the security
 * @property lotSize Lot size multiplier
 */
data class OrderUpdate(
    val dhanClientId: String? = null,
    val orderId: String? = null,
    val correlationId: String? = null,
    val orderStatus: OrderStatus? = null,
    val transactionType: TransactionType? = null,
    val exchangeSegment: ExchangeSegment? = null,
    val productType: ProductType? = null,
    val orderType: OrderType? = null,
    val validity: Validity? = null,
    val tradingSymbol: String? = null,
    val securityId: String? = null,
    val quantity: Int? = null,
    val disclosedQuantity: Int? = null,
    val price: Double? = null,
    val triggerPrice: Double? = null,
    val afterMarketOrder: Boolean? = null,
    val boProfitValue: Double? = null,
    val boStopLossValue: Double? = null,
    val legName: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val exchangeTime: String? = null,
    val drvExpiryDate: String? = null,
    val drvOptionType: String? = null,
    val drvStrikePrice: Double? = null,
    val omsErrorCode: String? = null,
    val omsErrorDescription: String? = null,
    val filledQty: Int? = null,
    val pendingQty: Int? = null,
    val cancelledQty: Int? = null,
    val tradedPrice: Double? = null,
    val instrument: String? = null,
    val isin: String? = null,
    val lotSize: Int? = null
) {
    /**
     * Checks if the order is in a final state.
     *
     * @return true if order is TRADED, CANCELLED, REJECTED, or EXPIRED
     */
    val isFinalState: Boolean
        get() = orderStatus in listOf(
            OrderStatus.TRADED,
            OrderStatus.CANCELLED,
            OrderStatus.REJECTED,
            OrderStatus.EXPIRED
        )

    /**
     * Checks if the order is pending.
     *
     * @return true if order is PENDING or PART_TRADED
     */
    val isPending: Boolean
        get() = orderStatus in listOf(
            OrderStatus.PENDING,
            OrderStatus.PART_TRADED
        )

    /**
     * Checks if the order has an error.
     *
     * @return true if omsErrorCode is not null or empty
     */
    val hasError: Boolean
        get() = !omsErrorCode.isNullOrBlank()
}

/**
 * Trade update data from WebSocket.
 *
 * Represents a trade execution event pushed by the server.
 *
 * ## Example
 *
 * ```kotlin
 * override fun onTradeUpdate(update: TradeUpdate) {
 *     println("Trade executed for order ${update.orderId}")
 *     println("  Symbol: ${update.tradingSymbol}")
 *     println("  ${update.transactionType}: ${update.tradedQuantity} @ ${update.tradedPrice}")
 *     println("  Exchange Trade ID: ${update.exchangeTradeId}")
 * }
 * ```
 *
 * @property dhanClientId User's Dhan client ID
 * @property orderId Associated order ID
 * @property exchangeOrderId Exchange-assigned order ID
 * @property exchangeTradeId Exchange-assigned trade ID
 * @property transactionType BUY or SELL
 * @property exchangeSegment Exchange and segment
 * @property productType Product type
 * @property tradingSymbol Exchange trading symbol
 * @property securityId Dhan security identifier
 * @property tradedQuantity Quantity traded in this execution
 * @property tradedPrice Price at which trade executed
 * @property createTime Trade timestamp
 * @property isin ISIN of the security
 */
data class TradeUpdate(
    val dhanClientId: String? = null,
    val orderId: String? = null,
    val exchangeOrderId: String? = null,
    val exchangeTradeId: String? = null,
    val transactionType: TransactionType? = null,
    val exchangeSegment: ExchangeSegment? = null,
    val productType: ProductType? = null,
    val tradingSymbol: String? = null,
    val securityId: String? = null,
    val tradedQuantity: Int? = null,
    val tradedPrice: Double? = null,
    val createTime: String? = null,
    val isin: String? = null
)

/**
 * Connection status response from WebSocket.
 *
 * @property type Message type
 * @property status Connection status (success/failure)
 * @property message Additional status message
 */
data class ConnectionStatus(
    val type: String? = null,
    val status: String? = null,
    val message: String? = null
) {
    /**
     * Checks if connection was successful.
     */
    val isSuccess: Boolean
        get() = status?.lowercase() == "success"
}
