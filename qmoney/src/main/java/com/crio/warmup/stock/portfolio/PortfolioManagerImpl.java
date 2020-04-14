package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from
  // main anymore.
  // Copy your code from Module#3
  // PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required
  // further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command
  // below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  // CHECKSTYLE:OFF
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) {

    List<AnnualizedReturn> annualizedReturn = new ArrayList<AnnualizedReturn>();
    for (PortfolioTrade itr : portfolioTrades) {
      String symbol = itr.getSymbol();
      LocalDate startDate = itr.getPurchaseDate();
      try {
        List<TiingoCandle> tiingoCandles = getStockQuote(symbol, startDate, endDate);
        Double buyPrice = tiingoCandles.get(0).getOpen();
        Double sellPrice = tiingoCandles.get(tiingoCandles.size() - 1).getClose();
        Double totalReturns = (sellPrice - buyPrice) / buyPrice;
        Double yearDiff = new Double(ChronoUnit.DAYS.between(startDate, endDate));
        yearDiff = yearDiff / 365.0;
        Double annualizedReturns = Math.pow((1 + totalReturns), 1.0 / yearDiff) - 1;
        annualizedReturn.add(new AnnualizedReturn(symbol, annualizedReturns, totalReturns));
      } catch (NullPointerException e) {
        e.printStackTrace();
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
    Comparator<AnnualizedReturn> compare = getComparator();
    annualizedReturn.sort(compare);
    return annualizedReturn;
  }






  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo thirdparty APIs to a separate function.
  // It should be split into fto parts.
  // Part#1 - Prepare the Url to call Tiingo based on a template constant,
  // by replacing the placeholders.
  // Constant should look like
  // https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  // Where ? are replaced with something similar to <ticker> and then actual url
  // produced by
  // replacing the placeholders with actual parameters.

  public List<TiingoCandle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    String s = buildUri(symbol, from, to);
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String result = restTemplate.getForObject(s, String.class);
    List<TiingoCandle> collection = mapper.readValue(result, new TypeReference<ArrayList<TiingoCandle>>() {
    });
    return collection;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    String token = "ab4e0d5458fdddec402b911cce9372c6b80ad01d";
    String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
        .replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());
    return url;
  }
}
