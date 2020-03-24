
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    byte[] byteArray = Files.readAllBytes(file.toPath());
    String content = new String(byteArray,"UTF-8");
    ObjectMapper mapper = getObjectMapper();
    PortfolioTrade[] trades = mapper.readValue(content, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<>();
    for (int i = 0; i < trades.length; i++) {
      symbols.add(trades[i].getSymbol());
    }
    return symbols;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader()
    .getResource(filename).toURI()).toFile();
  }





  // TODO: CRIO_TASK_MODULE_REST_API
  //  Copy the relavent code from #mainReadFile to parse the Json into PortfolioTrade list.
  //  Now That you have the list of PortfolioTrade already populated in module#1
  //  For each stock symbol in the portfolio trades,
  //  Call Tiingo api (https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=&endDate=&token=)
  //  with
  //   1. ticker = symbol in portfolio_trade
  //   2. startDate = purchaseDate in portfolio_trade.
  //   3. endDate = args[1]
  //  Use RestTemplate#getForObject in order to call the API,
  //  and deserialize the results in List<Candle>
  //  Note - You may have to register on Tiingo to get the api_token.
  //    Please refer the the module documentation for the steps.
  //  Find out the closing price of the stock on the end_date and
  //  return the list of all symbols in ascending order by its close value on endDate
  //  Test the function using gradle commands below
  //   ./gradlew run --args="trades.json 2020-01-01"
  //   ./gradlew run --args="trades.json 2019-07-01"
  //   ./gradlew run --args="trades.json 2019-12-03"
  //  And make sure that its printing correct results.

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    byte[] byteArray = Files.readAllBytes(file.toPath());
    String content = new String(byteArray,"UTF-8");
    LocalDate endDate = LocalDate.parse(args[1]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(content,PortfolioTrade[].class);

    String token = "ab4e0d5458fdddec402b911cce9372c6b80ad01d";
    String uri = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKRY";

    // List<TotalReturnsDto> totalReturnsDtoList = new ArrayList<TotalReturnsDto>();

    // for (int i = 0; i < portfolioTrades.length; i++) {
    //   String url = uri.replace("$APIKRY", token)
    //        .replace("$SYMBOL", portfolioTrades[i].getSymbol())
    //       .replace("$STARTDATE", portfolioTrades[i].getPurchaseDate().toString())
    //       .replace("$ENDDATE", endDate.toString());
    //   System.out.print(url);
    //   TiingoCandle[] tiingoCandles = new RestTemplate().getForObject(url, TiingoCandle[].class);

    //   TotalReturnsDto totalReturn = new TotalReturnsDto(portfolioTrades[i].getSymbol(), 
    //         Stream.of(tiingoCandles)
    //             .filter(candle -> candle.getDate().equals(endDate))
    //             .findFirst().get().getClose());

    //   totalReturnsDtoList.add(totalReturn);
    // }

    // totalReturnsDtoList.sort(Comparator.comparing(TotalReturnsDto::getClosingPrice));

    return Arrays.stream(portfolioTrades).map(trade -> {
      String url = uri.replace("$APIKRY", token).replace("$SYMBOL", trade.getSymbol())
          .replace("$STARTDATE", trade.getPurchaseDate().toString())
          .replace("$ENDDATE", endDate.toString());
      TiingoCandle[] tiingoCandles = new RestTemplate().getForObject(url, TiingoCandle[].class);
      return new TotalReturnsDto(trade.getSymbol(), Stream.of(tiingoCandles)
          .filter(candle -> candle.getDate().equals(endDate))
          .findFirst().get().getClose());
    }).sorted(Comparator.comparing(TotalReturnsDto::getClosingPrice))
        .map(TotalReturnsDto::getSymbol)
        .collect(Collectors.toList());
  }





  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadQuotes(args));


  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "qmoney/src/main/resources/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@66ac5762";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "22";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, 
      toStringOfObjectMapper, 
      functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }
}


