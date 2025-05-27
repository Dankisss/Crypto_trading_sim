package com.crypto_trading_sim.websocket;

import com.crypto_trading_sim.entity.CryptoPrice;
import com.crypto_trading_sim.repository.CryptoPriceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class KrakenWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(KrakenWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CryptoPriceRepository cryptoPriceRepository;
    private final PriorityQueue<CryptoPrice> topPrices = new PriorityQueue<>(20, Comparator.comparing(CryptoPrice::getLastPrice).reversed());
    private final Map<String, CryptoPrice> currentPrices = new HashMap<>();

    public KrakenWebSocketHandler(CryptoPriceRepository cryptoPriceRepository) {
        this.cryptoPriceRepository = cryptoPriceRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established with Kraken.");

        String subscriptionMessage = """
                {
                  "method": "subscribe",
                  "params": {
                    "channel": "ticker",
                    "symbol": ["BTC/USD", "ETH/USD", "XRP/USD", "LTC/USD", "ADA/USD", "SOL/USD", "DOT/USD", "DOGE/USD", "AVAX/USD", "MATIC/USD", "LINK/USD", "UNI/USD", "ATOM/USD", "XTZ/USD", "FIL/USD", "ETC/USD", "XLM/USD", "TRX/USD", "BCH/USD", "XMR/USD"]
                  }
                }
                """;

        session.sendMessage(new TextMessage(subscriptionMessage));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message.getPayload());

            if (rootNode.has("channel") && "ticker".equals(rootNode.get("channel").asText())) {
                JsonNode dataNode = rootNode.get("data");
                if (dataNode.isArray() && !dataNode.isEmpty()) {
                    for (JsonNode tickerData : dataNode) {
                        String symbol = tickerData.get("symbol").asText();
                        BigDecimal lastPrice = new BigDecimal(tickerData.get("last").asText());
                        LocalDateTime timestamp = LocalDateTime.now();

                        CryptoPrice cryptoPrice = new CryptoPrice(symbol, lastPrice, timestamp);

                        updateTopPrices(cryptoPrice);
                    }
                }
            } else if (rootNode.has("event") && "heartbeat".equals(rootNode.get("event").asText())) {
                log.info("Received heartbeat from Kraken.");
            } else {
                log.info("Received message: {}", message.getPayload());
            }

        } catch (JsonProcessingException e) {
            log.error("Error processing WebSocket message: {}", e.getMessage());
        }
    }

    private synchronized void updateTopPrices(CryptoPrice newPrice) {
        currentPrices.put(newPrice.getSymbol(), newPrice);

        topPrices.clear();
        for (CryptoPrice price : currentPrices.values()) {
            topPrices.offer(price);
            if (topPrices.size() > 20) {
                topPrices.poll();
            }
        }

        saveTopPrices();
    }

    private void saveTopPrices() {
        cryptoPriceRepository.deleteAll();
        cryptoPriceRepository.saveAll(new ArrayList<>(topPrices));

        log.info("Saved top {} prices to the database.", topPrices.size());
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        log.info("WebSocket connection closed: {}", status);
    }
}