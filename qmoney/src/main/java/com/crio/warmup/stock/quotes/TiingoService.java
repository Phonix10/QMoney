
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.web.client.HttpClientErrorException;



public class TiingoService implements StockQuotesService {

  private static String token = "717d713804ee57c4c0d4340edb6e9f0b34f40fe8";
  public static String getToken() {
    return token;
  }

  private RestTemplate restTemplate;


  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String url = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+token;
    return url;
  }


  @Override
public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException, StockQuoteServiceException {
    List<Candle> stocksStartToEndDate = new ArrayList<>();

    if (from.compareTo(to) >= 0) {
        throw new RuntimeException();
    }

    String url = buildUri(symbol, from, to);

    try {
        String stocks = restTemplate.getForObject(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        TiingoCandle[] stocksStartToEndDateArray = objectMapper.readValue(stocks, TiingoCandle[].class);
        stocksStartToEndDate = Arrays.asList(stocksStartToEndDateArray);
    } catch (NullPointerException e) {
        throw new StockQuoteServiceException(
                "Error occurred when requesting response from Tiingo API",
                e.getCause());
    }

    return stocksStartToEndDate;
}


  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }







  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

}
