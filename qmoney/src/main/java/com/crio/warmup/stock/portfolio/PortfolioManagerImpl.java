
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  private static String token = "717d713804ee57c4c0d4340edb6e9f0b34f40fe8";
  public static String getToken() {
    return token;
  }


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  protected PortfolioManagerImpl(StockQuotesService stockQuotesService){
    this.stockQuotesService = stockQuotesService;
  }








  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to){
    String url = buildUri(symbol, from, to);
    TiingoCandle[] tiingoCandleArray = restTemplate.getForObject(url, TiingoCandle[].class);
    return Arrays.stream(tiingoCandleArray).collect(Collectors.toList());
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String url = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+token;
    return url;
  }

  private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size()-1).getClose();
  }

  private AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        double totalNumberOfYears = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365.2422;
        double totalReturns = (sellPrice-buyPrice)/buyPrice;
        double annualized_Return = Math.pow((1.0 + totalReturns), (1.0 / totalNumberOfYears)) - 1;
      return new AnnualizedReturn(trade.getSymbol(), annualized_Return, totalReturns);
    }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws StockQuoteServiceException {
        List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
        
        for (PortfolioTrade trade : portfolioTrades) {
          List<Candle> candles = new ArrayList<>();
          try{
          candles = stockQuotesService.getStockQuote(trade.getSymbol(),trade.getPurchaseDate(),endDate);
          }catch(JsonProcessingException e){
            e.printStackTrace();
          }
          AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, 
          getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));
          annualizedReturns.add(annualizedReturn);
          }
          return annualizedReturns.stream()
          .sorted((a1, a2) -> Double.compare(a2.getAnnualizedReturn(), a1.getAnnualizedReturn()))
          .collect(Collectors.toList());
  }
}
