package com.crypto_trading_sim.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    private static final String KRAKEN_API_URL = "https://api.kraken.com/0/public/";

    private static String makeApiRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != HttpStatus.OK.value()) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        conn.disconnect();
        return sb.toString();
    }

    private static List<String> getUsdAssetPairs() throws Exception {
        String url = KRAKEN_API_URL + "AssetPairs";

        String response = makeApiRequest(url);
        JSONObject jsonResponse = new JSONObject(response);
        List<String> pairs = new ArrayList<>();

        if (jsonResponse.has("result")) {
            JSONObject result = jsonResponse.getJSONObject("result");
            for (String pairName : result.keySet()) {
                JSONObject pairDetails = result.getJSONObject(pairName);

                if (pairDetails.has("quote") && pairDetails.get("quote").toString().endsWith("USD")) {
                    pairs.add(pairName);
                }
            }
        }

        return pairs;
    }

    private static Map<String, Double> getCryptoPrices() throws Exception {
        Map<String, Double> cryptoPrices = new HashMap<>();

        List<String> pairs = getUsdAssetPairs();
        String pairsParam = String.join(",", pairs);

        String url = KRAKEN_API_URL + "Ticker?pair=" + pairsParam;

        String response = makeApiRequest(url);
        JSONObject jsonResponse = new JSONObject(response);

        if (jsonResponse.has("result")) {

            JSONObject result = jsonResponse.getJSONObject("result");
            for (String pairName : result.keySet()) {
                JSONObject tickerInfo = result.getJSONObject(pairName);
                if (tickerInfo.has("c")) {
                    JSONArray cArray = tickerInfo.getJSONArray("c");

                    if (!cArray.isEmpty()) {
                        double price = cArray.getDouble(0);
                        String baseCurrency = pairName.replace("USD", "").replace("XBT", "BTC");

                        cryptoPrices.put(baseCurrency, price);
                    }
                }
            }
        }

        return cryptoPrices;
    }

    public List<Map.Entry<String, Double>> fetchResults() throws Exception {
        Map<String, Double> prices = getCryptoPrices();

        List<Map.Entry<String, Double>> sortedCrypto = prices.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .toList();

        List<Map.Entry<String, Double>> top20 = sortedCrypto.stream()
                .limit(20)
                .toList();

        return top20;
    }

}
