package com.crypto_trading_sim.websocket;

import com.crypto_trading_sim.repository.CryptoPriceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class WebSocketConfig {

    private final CryptoPriceRepository cryptoPriceRepository;

    public WebSocketConfig(CryptoPriceRepository cryptoPriceRepository) {
        this.cryptoPriceRepository = cryptoPriceRepository;
    }

    @Bean
    public WebSocketHandler krakenWebSocketHandler() {
        return new KrakenWebSocketHandler(cryptoPriceRepository);
    }

    @Bean
    public WebSocketConnectionManager webSocketConnectionManager() {
        WebSocketConnectionManager manager = new WebSocketConnectionManager(
                new StandardWebSocketClient(),
                krakenWebSocketHandler(),
                "wss://ws.kraken.com/v2"
        );
        manager.setAutoStartup(true);
        return manager;
    }

}