package io.github.sonicalgo.dhan.model.enums

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Exchange segment identifiers supported by Dhan.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class ExchangeSegment(val code: Int) {
    /** Index Value */
    @JsonProperty("IDX_I")
    IDX_I(0),

    /** NSE Equity Cash */
    @JsonProperty("NSE_EQ")
    NSE_EQ(1),

    /** NSE Futures & Options */
    @JsonProperty("NSE_FNO")
    NSE_FNO(2),

    /** NSE Currency */
    @JsonProperty("NSE_CURRENCY")
    NSE_CURRENCY(3),

    /** BSE Equity Cash */
    @JsonProperty("BSE_EQ")
    BSE_EQ(4),

    /** MCX Commodity */
    @JsonProperty("MCX_COMM")
    MCX_COMM(5),

    /** BSE Currency */
    @JsonProperty("BSE_CURRENCY")
    BSE_CURRENCY(7),

    /** BSE Futures & Options */
    @JsonProperty("BSE_FNO")
    BSE_FNO(8);

    companion object {
        fun fromCode(code: Int): ExchangeSegment? = entries.find { it.code == code }
    }
}

/**
 * Transaction type for orders.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class TransactionType {
    /** Buy transaction */
    @JsonProperty("BUY")
    BUY,

    /** Sell transaction */
    @JsonProperty("SELL")
    SELL
}

/**
 * Product types for orders.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class ProductType {
    /** Cash & Carry for equity deliveries */
    @JsonProperty("CNC")
    CNC,

    /** Intraday for Equity, Futures & Options */
    @JsonProperty("INTRADAY")
    INTRADAY,

    /** Carry Forward in Futures & Options */
    @JsonProperty("MARGIN")
    MARGIN,

    /** Margin Trading Facility */
    @JsonProperty("MTF")
    MTF,

    /** Cover Order - valid intraday only */
    @JsonProperty("CO")
    CO,

    /** Bracket Order - valid intraday only */
    @JsonProperty("BO")
    BO
}

/**
 * Order types supported by Dhan.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class OrderType {
    /** Limit order */
    @JsonProperty("LIMIT")
    LIMIT,

    /** Market order */
    @JsonProperty("MARKET")
    MARKET,

    /** Stop Loss Limit order */
    @JsonProperty("STOP_LOSS")
    STOP_LOSS,

    /** Stop Loss Market order */
    @JsonProperty("STOP_LOSS_MARKET")
    STOP_LOSS_MARKET
}

/**
 * Order validity types.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class Validity {
    /** Day order - valid for the trading day */
    @JsonProperty("DAY")
    DAY,

    /** Immediate or Cancel */
    @JsonProperty("IOC")
    IOC
}

/**
 * Order status values.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class OrderStatus {
    /** Did not reach the exchange server */
    @JsonProperty("TRANSIT")
    TRANSIT,

    /** Awaiting execution at exchange */
    @JsonProperty("PENDING")
    PENDING,

    /** Super Order with both entry and exit placed */
    @JsonProperty("CLOSED")
    CLOSED,

    /** Super Order with Target or Stop Loss activated */
    @JsonProperty("TRIGGERED")
    TRIGGERED,

    /** Rejected by broker or exchange */
    @JsonProperty("REJECTED")
    REJECTED,

    /** Cancelled by user */
    @JsonProperty("CANCELLED")
    CANCELLED,

    /** Partial quantity executed */
    @JsonProperty("PART_TRADED")
    PART_TRADED,

    /** Successfully executed */
    @JsonProperty("TRADED")
    TRADED,

    /** Order expired without execution */
    @JsonProperty("EXPIRED")
    EXPIRED,

    /** Forever Order confirmed */
    @JsonProperty("CONFIRM")
    CONFIRM
}

/**
 * After Market Order timing options.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class AmoTime {
    /** Pumped at pre-market session */
    @JsonProperty("PRE_OPEN")
    PRE_OPEN,

    /** Pumped at market open */
    @JsonProperty("OPEN")
    OPEN,

    /** Pumped 30 minutes after open */
    @JsonProperty("OPEN_30")
    OPEN_30,

    /** Pumped 60 minutes after open */
    @JsonProperty("OPEN_60")
    OPEN_60
}

/**
 * Option type for derivatives.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class DrvOptionType {
    /** Call Option */
    @JsonProperty("CALL")
    CALL,

    /** Put Option */
    @JsonProperty("PUT")
    PUT
}

/**
 * Position type for portfolio positions.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class PositionType {
    /** Long position */
    @JsonProperty("LONG")
    LONG,

    /** Short position */
    @JsonProperty("SHORT")
    SHORT,

    /** Closed position */
    @JsonProperty("CLOSED")
    CLOSED
}

/**
 * Forever Order flag types.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class ForeverOrderFlag {
    /** Single Forever Order */
    @JsonProperty("SINGLE")
    SINGLE,

    /** One Cancels Other */
    @JsonProperty("OCO")
    OCO
}

/**
 * Leg name for multi-leg orders.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class LegName {
    /** Entry leg of the order */
    @JsonProperty("ENTRY_LEG")
    ENTRY_LEG,

    /** Target leg for profit booking */
    @JsonProperty("TARGET_LEG")
    TARGET_LEG,

    /** Stop loss leg */
    @JsonProperty("STOP_LOSS_LEG")
    STOP_LOSS_LEG
}

/**
 * Instrument types supported by Dhan.
 *
 * @see <a href="https://dhanhq.co/docs/v2/annexure/">DhanHQ Annexure</a>
 */
enum class InstrumentType {
    /** Index */
    @JsonProperty("INDEX")
    INDEX,

    /** Index Futures */
    @JsonProperty("FUTIDX")
    FUTIDX,

    /** Index Options */
    @JsonProperty("OPTIDX")
    OPTIDX,

    /** Equity */
    @JsonProperty("EQUITY")
    EQUITY,

    /** Stock Futures */
    @JsonProperty("FUTSTK")
    FUTSTK,

    /** Stock Options */
    @JsonProperty("OPTSTK")
    OPTSTK,

    /** Commodity Futures */
    @JsonProperty("FUTCOM")
    FUTCOM,

    /** Option on Futures */
    @JsonProperty("OPTFUT")
    OPTFUT,

    /** Currency Futures */
    @JsonProperty("FUTCUR")
    FUTCUR,

    /** Currency Options */
    @JsonProperty("OPTCUR")
    OPTCUR
}

/**
 * Kill switch status for Trader's Control.
 *
 * @see <a href="https://dhanhq.co/docs/v2/traders-control/">DhanHQ Trader's Control</a>
 */
enum class KillSwitchStatus {
    /** Activate kill switch - disable trading */
    @JsonProperty("ACTIVATE")
    ACTIVATE,

    /** Deactivate kill switch - enable trading */
    @JsonProperty("DEACTIVATE")
    DEACTIVATE
}

/**
 * IP flag for static IP configuration.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class IpFlag {
    /** Primary IP address */
    @JsonProperty("PRIMARY")
    PRIMARY,

    /** Secondary IP address */
    @JsonProperty("SECONDARY")
    SECONDARY
}

/**
 * EDIS transaction status.
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS</a>
 */
enum class EdisStatus {
    /** Transaction successful */
    @JsonProperty("SUCCESS")
    SUCCESS,

    /** Transaction failed */
    @JsonProperty("FAILURE")
    FAILURE,

    /** Transaction pending */
    @JsonProperty("PENDING")
    PENDING
}

/**
 * Exchange for EDIS transactions.
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS</a>
 */
enum class Exchange {
    /** National Stock Exchange */
    @JsonProperty("NSE")
    NSE,

    /** Bombay Stock Exchange */
    @JsonProperty("BSE")
    BSE
}

/**
 * Segment for EDIS transactions.
 *
 * @see <a href="https://dhanhq.co/docs/v2/edis/">DhanHQ EDIS</a>
 */
enum class Segment {
    /** Equity segment */
    @JsonProperty("EQ")
    EQ
}

/**
 * Historical data interval options.
 *
 * @see <a href="https://dhanhq.co/docs/v2/historical-data/">DhanHQ Historical Data</a>
 */
enum class ChartInterval(val minutes: Int) {
    /** 1 minute candles */
    @JsonProperty("1")
    MINUTE_1(1),

    /** 5 minute candles */
    @JsonProperty("5")
    MINUTE_5(5),

    /** 15 minute candles */
    @JsonProperty("15")
    MINUTE_15(15),

    /** 25 minute candles */
    @JsonProperty("25")
    MINUTE_25(25),

    /** 60 minute candles */
    @JsonProperty("60")
    MINUTE_60(60);

    companion object {
        fun fromMinutes(minutes: Int): ChartInterval? = entries.find { it.minutes == minutes }
    }
}

/**
 * DDPI status for user profile.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class DdpiStatus {
    /** DDPI is active */
    @JsonProperty("Active")
    ACTIVE,

    /** DDPI is deactive */
    @JsonProperty("Deactive")
    DEACTIVE
}

/**
 * MTF status for user profile.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class MtfStatus {
    /** MTF is active */
    @JsonProperty("Active")
    ACTIVE,

    /** MTF is deactive */
    @JsonProperty("Deactive")
    DEACTIVE
}

/**
 * Data plan status for user profile.
 *
 * @see <a href="https://dhanhq.co/docs/v2/authentication/">DhanHQ Authentication</a>
 */
enum class DataPlanStatus {
    /** Data plan is active */
    @JsonProperty("Active")
    ACTIVE,

    /** Data plan is deactive */
    @JsonProperty("Deactive")
    DEACTIVE
}
