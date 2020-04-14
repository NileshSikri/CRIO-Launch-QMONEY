package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication_Local_709 {

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();

    List<String> symbols = new ArrayList<>();
    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);

    for (int i = 0; i < trades.length; i++) {
      symbols.add(trades[i].getSymbol());
    }

    return symbols;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication_Local_709.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread()
    .getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = 
        "qmoney/src/main/resources/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@362045c0";
    String functionNameFromTestFileInStackTrace = "mainReadFile()";
    String lineNumberFromTestFileInStackTrace = 
        "22";

    return Arrays.asList(new String[] { valueOfArgument0, 
      resultOfResolveFilePathArgs0, toStringOfObjectMapper,
        functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }

  public static class SortingObject {
    private String symbol;
    private double close;

    public SortingObject() {
    }

    public SortingObject(String s, double c) {
      this.symbol = s;
      this.close = c;
    }

    public String getSymbol() {
      return symbol;
    }

    public void setSymbol(String symbol) {
      this.symbol = symbol;
    }

    public double getClose() {
      return close;
    }

    public void setClose(double close) {
      this.close = close;
    }
  }

  public static double getOpenVal(String ticker, LocalDate startDate) {
    final String url = "https://api.tiingo.com/tiingo/daily/" + ticker + "/prices?startDate=" + startDate + "&endDate="
        + startDate + "&token=ab4e0d5458fdddec402b911cce9372c6b80ad01d";

    RestTemplate restTemplate = new RestTemplate();
    try {
      TiingoCandle[] result = restTemplate.getForObject(url, TiingoCandle[].class);
      if (result != null) {
        int size = result.length - 1;

        if (size >= 0) {
          return result[0].getOpen();
        } else {
          throw new RuntimeException("Invalid dates");
        }
      } else {
        throw new NullPointerException("NPE");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static double getCloseVal(String ticker, String startDate, String endDate) {
    final String url = "https://api.tiingo.com/tiingo/daily/" + ticker + "/prices?startDate=" + startDate + "&endDate="
        + endDate + "&token=ab4e0d5458fdddec402b911cce9372c6b80ad01d";

    RestTemplate restTemplate = new RestTemplate();
    try {
      TiingoCandle[] result = restTemplate.getForObject(url, TiingoCandle[].class);
      if (result != null) {
        int size = result.length - 1;

        if (size >= 0) {
          return result[size].getClose();
        } else {
          throw new RuntimeException("Invalid dates");
        }
      } else {
        throw new NullPointerException("NPE");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();

    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);

    List<SortingObject> list = new ArrayList<>();

    for (int i = 0; i < trades.length; i++) {
      SortingObject s = new SortingObject();
      s.setSymbol(trades[i].getSymbol());
      s.setClose(getCloseVal(trades[i].getSymbol(), 
          trades[i].getPurchaseDate().toString(), args[1]));

      list.add(s);
    }

    Collections.sort(list, (s1, s2) -> {
      if (s1 == s2) {
        return 0;
      }
      return (s1.getClose() > s2.getClose()) ? 1 : -1;
    });

    List<String> symbols = new ArrayList<>();

    for (int i = 0; i < list.size(); i++) {
      symbols.add(list.get(i).getSymbol());
    }

    return symbols;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS =========================>>>>(DONE)
  //  Copy the relevant code from #mainReadQuotes to parse the Json into PortfolioTrade list and
  //  Get the latest quotes from TIingo.
  //  Now That you have the list of PortfolioTrade And their data,
  //  With this data, Calculate annualized returns for the stocks provided in the Json
  //  Below are the values to be considered for calculations.
  //  buy_price = open_price on purchase_date and sell_value = close_price on end_date
  //  startDate and endDate are already calculated in module2
  //  using the function you just wrote #calculateAnnualizedReturns
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
      
    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();

    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
      
    List<SortingObject> list = new ArrayList<>();

    for (int i = 0; i < trades.length; i++) {
      SortingObject s = new SortingObject();
      s.setSymbol(trades[i].getSymbol());
      s.setClose(getCloseVal(trades[i].getSymbol(), 
          trades[i].getPurchaseDate().toString(), args[1]));

      list.add(s);
    }

    List<AnnualizedReturn> liAnnualizedReturns = new ArrayList<>();

    for (int i = 0; i < list.size(); i++) {
      SortingObject so = list.get(i);
      LocalDate endDate = LocalDate.parse(args[1]);
      PortfolioTrade trade = trades[i];
      double buyPrice = getOpenVal(trade.getSymbol(), trade.getPurchaseDate());

      AnnualizedReturn ar = calculateAnnualizedReturns(endDate, trade, buyPrice, so.getClose());
      liAnnualizedReturns.add(ar);
    }

    Collections.sort(liAnnualizedReturns, (a1, a2) -> {
      if (a1 == a2) {
        return 0;
      }
      return (a1.getAnnualizedReturn() < a2.getAnnualizedReturn()) ? 1 : -1;
    });

    return liAnnualizedReturns;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS =========================>>>>(DONE)
  //  annualized returns should be calculated in two steps -
  //  1. Calculate totalReturn = (sell_value - buy_value) / buy_value
  //  Store the same as totalReturns
  //  2. calculate extrapolated annualized returns by scaling the same in years span. The formula is
  //  annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //  Store the same as annualized_returns
  //  return the populated list of AnnualizedReturn for all stocks,
  //  Test the same using below specified command. The build should be successful
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    long numDays = Duration.between(trade.getPurchaseDate().atStartOfDay(), 
        endDate.atStartOfDay()).toDays();
    double totalNumYears = numDays / 365d;

    double totalReturns = (sellPrice - buyPrice) / buyPrice;
    double annualizedReturns = Math.pow((1 + totalReturns), (1d / totalNumYears)) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturns);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());



    printJsonObject(mainCalculateSingleReturn(args));

  }
}