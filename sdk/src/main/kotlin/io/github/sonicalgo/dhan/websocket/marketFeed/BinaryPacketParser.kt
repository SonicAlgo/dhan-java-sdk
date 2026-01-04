package io.github.sonicalgo.dhan.websocket.marketFeed

import io.github.sonicalgo.dhan.config.DhanConstants
import okio.ByteString
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Parser for binary market feed packets from Dhan WebSocket.
 *
 * Parses the binary protocol used by Dhan's market feed WebSocket.
 * Each packet starts with a 1-byte packet type indicator followed
 * by a 2-byte header field (H), then type-specific payload.
 *
 * Packet format (little-endian): B (packet type) + H (2 bytes) + payload
 *
 * Packet types (from [DhanConstants.FeedResponseCode]):
 * - 1 (INDEX): Index data
 * - 2 (TICKER): Ticker data
 * - 4 (QUOTE): Quote data with market depth
 * - 5 (OI): Open interest update
 * - 6 (PREV_CLOSE): Previous close update
 * - 7 (MARKET_STATUS): Market status update
 * - 8 (FULL): Full data with OI
 * - 50 (DISCONNECT): Server disconnect
 *
 * @see <a href="https://dhanhq.co/docs/v2/live-market-feed/">DhanHQ Live Market Feed</a>
 */
internal object BinaryPacketParser {

    /**
     * Converts Float to formatted String with 2 decimal places.
     *
     * The Dhan API sends prices as 32-bit floats, which cannot exactly represent
     * values like 9.69. This extension formats to 2 decimal places for clean display
     * (e.g., "9.70" instead of "9.699999809265137").
     */
    private fun Float.toFormattedPrice(): String {
        return "%.2f".format(this)
    }

    // Helper data class for common header
    private data class PacketHeader(val exchangeSegment: Int, val securityId: String)

    /**
     * Parses common header: H(2) + B(1) + I(4) = 7 bytes after packet type.
     */
    private fun parseHeader(buffer: ByteBuffer): PacketHeader {
        buffer.getShort()  // Skip 2-byte H field (unknown purpose)
        val exchangeSegment = buffer.get().toInt() and 0xFF
        val securityId = buffer.getInt().toString()
        return PacketHeader(exchangeSegment, securityId)
    }

    /**
     * Parses a binary packet and invokes the appropriate listener callback.
     *
     * @param bytes Raw binary data from WebSocket
     * @param listener Listener to receive parsed data
     */
    fun parse(bytes: ByteString, listener: MarketFeedListener) {
        try {
            val buffer = ByteBuffer.wrap(bytes.toByteArray())
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // First byte indicates packet type (B in Python struct format)
            when (val packetType = buffer.get().toInt() and 0xFF) {
                DhanConstants.FeedResponseCode.INDEX -> parseIndex(buffer, listener)
                DhanConstants.FeedResponseCode.TICKER -> parseTicker(buffer, listener)
                DhanConstants.FeedResponseCode.QUOTE -> parseQuote(buffer, listener)
                DhanConstants.FeedResponseCode.OI -> parseOI(buffer, listener)
                DhanConstants.FeedResponseCode.PREV_CLOSE -> parsePrevClose(buffer, listener)
                DhanConstants.FeedResponseCode.MARKET_STATUS -> parseMarketStatus(buffer, listener)
                DhanConstants.FeedResponseCode.FULL -> parseFull(buffer, listener)
                DhanConstants.FeedResponseCode.DISCONNECT -> parseDisconnect(buffer, listener)
                else -> {
                    listener.onError(RuntimeException("Unknown packet type: $packetType"))
                }
            }
        } catch (e: Exception) {
            listener.onError(e)
        }
    }

    private fun parseIndex(buffer: ByteBuffer, listener: MarketFeedListener) {
        val header = parseHeader(buffer)

        val indexValue = buffer.getFloat().toFormattedPrice()
        val openValue = buffer.getFloat().toFormattedPrice()
        val highValue = buffer.getFloat().toFormattedPrice()
        val lowValue = buffer.getFloat().toFormattedPrice()
        val closeValue = buffer.getFloat().toFormattedPrice()
        val changePercent = buffer.getFloat().toFormattedPrice()

        listener.onIndexData(
            IndexData(
                exchangeSegment = header.exchangeSegment,
                securityId = header.securityId,
                indexValue = indexValue,
                openValue = openValue,
                highValue = highValue,
                lowValue = lowValue,
                closeValue = closeValue,
                changePercent = changePercent
            )
        )
    }

    /**
     * Parses ticker packet (16 bytes total).
     * Format: `<BHBIfI>` = packet_type(1) + H(2) + exchange(1) + security_id(4) + LTP(4) + LTT(4)
     */
    private fun parseTicker(buffer: ByteBuffer, listener: MarketFeedListener) {
        val header = parseHeader(buffer)

        // Only LTP (float) and LTT (int) after header
        val ltp = buffer.getFloat().toFormattedPrice()
        val ltt = buffer.getInt().toLong() and 0xFFFFFFFFL

        listener.onTickerData(
            TickerData(
                exchangeSegment = header.exchangeSegment,
                securityId = header.securityId,
                ltp = ltp,
                ltt = ltt
            )
        )
    }

    /**
     * Parses quote packet (50 bytes total).
     * Format: `<BHBIfHIfIIIffff>` = header(8) + LTP(4) + LTQ(2) + LTT(4) + avgPrice(4) +
     *         volume(4) + totalSellQty(4) + totalBuyQty(4) + open(4) + close(4) + high(4) + low(4)
     */
    private fun parseQuote(buffer: ByteBuffer, listener: MarketFeedListener) {
        val header = parseHeader(buffer)

        val ltp = buffer.getFloat().toFormattedPrice()
        val ltq = buffer.getShort().toInt() and 0xFFFF
        val ltt = buffer.getInt().toLong() and 0xFFFFFFFFL
        val avgPrice = buffer.getFloat().toFormattedPrice()
        val volume = buffer.getInt().toLong() and 0xFFFFFFFFL
        val totalSellQuantity = buffer.getInt().toLong() and 0xFFFFFFFFL
        val totalBuyQuantity = buffer.getInt().toLong() and 0xFFFFFFFFL
        val openPrice = buffer.getFloat().toFormattedPrice()
        val closePrice = buffer.getFloat().toFormattedPrice()
        val highPrice = buffer.getFloat().toFormattedPrice()
        val lowPrice = buffer.getFloat().toFormattedPrice()

        listener.onQuoteData(
            QuoteData(
                exchangeSegment = header.exchangeSegment,
                securityId = header.securityId,
                ltp = ltp,
                ltq = ltq,
                ltt = ltt,
                avgPrice = avgPrice,
                volume = volume,
                totalBuyQuantity = totalBuyQuantity,
                totalSellQuantity = totalSellQuantity,
                openPrice = openPrice,
                closePrice = closePrice,
                highPrice = highPrice,
                lowPrice = lowPrice
            )
        )
    }

    /**
     * Parses full packet (162 bytes total).
     * Format: `<BHBIfHIfIIIIIIffff100s>` = header(8) + quote fields + OI fields + depth(100)
     */
    private fun parseFull(buffer: ByteBuffer, listener: MarketFeedListener) {
        val header = parseHeader(buffer)

        // Quote fields
        val ltp = buffer.getFloat().toFormattedPrice()
        val ltq = buffer.getShort().toInt() and 0xFFFF
        val ltt = buffer.getInt().toLong() and 0xFFFFFFFFL
        val avgPrice = buffer.getFloat().toFormattedPrice()
        val volume = buffer.getInt().toLong() and 0xFFFFFFFFL
        val totalSellQuantity = buffer.getInt().toLong() and 0xFFFFFFFFL
        val totalBuyQuantity = buffer.getInt().toLong() and 0xFFFFFFFFL

        // OI fields
        val openInterest = buffer.getInt().toLong() and 0xFFFFFFFFL
        val oiDayHigh = buffer.getInt().toLong() and 0xFFFFFFFFL
        val oiDayLow = buffer.getInt().toLong() and 0xFFFFFFFFL

        // OHLC
        val openPrice = buffer.getFloat().toFormattedPrice()
        val closePrice = buffer.getFloat().toFormattedPrice()
        val highPrice = buffer.getFloat().toFormattedPrice()
        val lowPrice = buffer.getFloat().toFormattedPrice()

        // Parse 100 bytes of depth data (5 levels)
        // Each level: bid_qty(4) + ask_qty(4) + bid_orders(2) + ask_orders(2) + bid_price(4) + ask_price(4) = 20 bytes
        // 5 levels × 20 bytes = 100 bytes
        val bids = mutableListOf<DepthLevel>()
        val asks = mutableListOf<DepthLevel>()

        repeat(5) {
            val bidQty = buffer.getInt()
            val askQty = buffer.getInt()
            val bidOrders = buffer.getShort().toInt() and 0xFFFF
            val askOrders = buffer.getShort().toInt() and 0xFFFF
            val bidPrice = buffer.getFloat().toFormattedPrice()
            val askPrice = buffer.getFloat().toFormattedPrice()

            bids.add(DepthLevel(bidPrice, bidQty, bidOrders))
            asks.add(DepthLevel(askPrice, askQty, askOrders))
        }

        listener.onFullData(
            FullData(
                exchangeSegment = header.exchangeSegment,
                securityId = header.securityId,
                ltp = ltp,
                ltq = ltq,
                ltt = ltt,
                avgPrice = avgPrice,
                volume = volume,
                totalBuyQuantity = totalBuyQuantity,
                totalSellQuantity = totalSellQuantity,
                openPrice = openPrice,
                closePrice = closePrice,
                highPrice = highPrice,
                lowPrice = lowPrice,
                openInterest = openInterest,
                oiDayHigh = oiDayHigh,
                oiDayLow = oiDayLow,
                bids = bids,
                asks = asks
            )
        )
    }

    private fun parseOI(buffer: ByteBuffer, listener: MarketFeedListener) {
        val header = parseHeader(buffer)
        val openInterest = buffer.getInt().toLong() and 0xFFFFFFFFL

        listener.onOIData(
            OIData(
                exchangeSegment = header.exchangeSegment,
                securityId = header.securityId,
                openInterest = openInterest
            )
        )
    }

    private fun parsePrevClose(buffer: ByteBuffer, listener: MarketFeedListener) {
        val header = parseHeader(buffer)
        val previousClose = buffer.getFloat().toFormattedPrice()

        listener.onPrevCloseData(
            PrevCloseData(
                exchangeSegment = header.exchangeSegment,
                securityId = header.securityId,
                previousClose = previousClose
            )
        )
    }

    private fun parseMarketStatus(buffer: ByteBuffer, listener: MarketFeedListener) {
        // Format: <BHBI> - skip H(2) after packet type, then B(1) for exchange
        buffer.getShort()  // Skip 2-byte H field
        val exchangeSegment = buffer.get().toInt() and 0xFF
        // Read status from I(4) field - only using first byte
        val statusByte = buffer.get().toInt() and 0xFF
        buffer.position(buffer.position() + 3)  // Skip remaining 3 bytes of I field

        val status = when (statusByte) {
            1 -> "OPEN"
            2 -> "CLOSED"
            3 -> "PRE_OPEN"
            4 -> "POST_CLOSE"
            else -> "UNKNOWN"
        }

        listener.onMarketStatus(
            MarketStatusData(
                exchangeSegment = exchangeSegment,
                status = status
            )
        )
    }

    /**
     * Parses a disconnect packet from the server.
     *
     * Disconnect packet format (Python struct `<BHBIH>`, 10 bytes total):
     * - B (1 byte): Packet type (50) - already consumed
     * - H (2 bytes): Unknown
     * - B (1 byte): Unknown
     * - I (4 bytes): Unknown
     * - H (2 bytes): Disconnect reason code
     *
     * After reading packet type, 9 bytes remain. Skip 7 bytes to reach reason code.
     *
     * @param buffer ByteBuffer positioned after packet type
     * @param listener Listener to notify of disconnect error
     */
    private fun parseDisconnect(buffer: ByteBuffer, listener: MarketFeedListener) {
        try {
            // Format: <BHBIH> - after 1-byte packet type, we have 9 bytes remaining
            // Skip H(2) + B(1) + I(4) = 7 bytes to reach reason code
            if (buffer.remaining() >= 9) {
                buffer.position(buffer.position() + 7)
                val reasonCode = buffer.getShort().toInt() and 0xFFFF
                listener.onError(RuntimeException("Server disconnected (code $reasonCode)"))
            } else if (buffer.remaining() >= 2) {
                // Fallback: try reading last 2 bytes as reason code
                buffer.position(buffer.position() + buffer.remaining() - 2)
                val reasonCode = buffer.getShort().toInt() and 0xFFFF
                listener.onError(RuntimeException("Server disconnected (code $reasonCode)"))
            } else {
                // No reason code available
                listener.onError(RuntimeException("Server requested disconnect"))
            }
        } catch (e: Exception) {
            // Parsing failed - report generic disconnect
            listener.onError(e)
        }
    }
}
