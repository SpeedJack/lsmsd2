package app.scraper.net;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import app.datamodel.Candle;
import app.scraper.net.data.APICandle;
import app.scraper.net.data.APIMarket;
import app.scraper.net.data.ExchangeInfo;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BinanceConnector implements SourceConnector
{
	private Retrofit retrofit;
	private BinanceInterface apiInterface;
	private int lastRequestMinute;
	private int usedWeight;
	private final int availableWeight = 1200;
	private final double requestMargin = 0.25;
	private int requestedWaitTime;
	private List<APICandle> candles;
	
	private class CandleDeserializer implements JsonDeserializer<APICandle>
	{
		@Override
		public APICandle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
		{
			JsonArray candle = json.getAsJsonArray();
			return new APICandle(
				Instant.ofEpochMilli(candle.get(0).getAsLong()),
				Double.parseDouble(candle.get(1).getAsString()),
				Double.parseDouble(candle.get(2).getAsString()),
				Double.parseDouble(candle.get(3).getAsString()),
				Double.parseDouble(candle.get(4).getAsString()),
				Double.parseDouble(candle.get(5).getAsString())
				);
		}
		
	}
	
	public BinanceConnector()
	{
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
		Gson gson = new GsonBuilder().registerTypeAdapter(APICandle.class, new CandleDeserializer()).create();
		retrofit = new Retrofit.Builder().baseUrl("https://api.binance.com/api/v3/").client(client).addConverterFactory(GsonConverterFactory.create(gson)).build();
		apiInterface = retrofit.create(BinanceInterface.class);
		candles = new ArrayList<APICandle>();
	}
	
	private void rateLimit() throws InterruptedException
	{
		Thread.sleep(3000);
		LocalTime curTime = LocalTime.now();
		int curMinute = curTime.getHour() * 60 + curTime.getMinute();
		int remainingSeconds = 60 - curTime.getSecond();
		if (lastRequestMinute == curMinute) {
			double curAvailWeight = (availableWeight * (1 - requestMargin)) - usedWeight;
			if (curAvailWeight <= 0)
				Thread.sleep(remainingSeconds);
			else
				Thread.sleep((long)Math.ceil(curAvailWeight / remainingSeconds));
		}
		if (requestedWaitTime > 0)
			Thread.sleep(requestedWaitTime);
	}
	
	@Override
	public List<APIMarket> getMarkets() throws InterruptedException
	{
		rateLimit();
		Call<ExchangeInfo> call = apiInterface.getExchangeInfo();
		
		Response<ExchangeInfo> response;
		try {
			response = call.execute();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String weight = response.headers().get("x-mbx-used-weight-1m");
		if (weight != null)
			usedWeight = Integer.parseInt(weight);
		else
			usedWeight++;
		return response.body().getMarkets();
	}
	
	private String getIntervalString(int minutes)
	{
		if (minutes == 60 * 24 * 7)
			return "1w";
		if (minutes >= 60 * 24) {
			if (minutes % 60 * 24 != 0)
				throw new IllegalArgumentException();
			return (minutes / (60 * 24)) + "d";
		}
		if (minutes >= 60) {
			if (minutes % 60 != 0)
				throw new IllegalArgumentException();
			return (minutes / 60) + "h";
		}
		return minutes + "m";
	}

	@Override
	public List<APICandle> getLastCandles(String marketId, int granularity, Instant start) throws InterruptedException
	{
		return getCandles(marketId, granularity, start);
	}

	protected List<APICandle> getCandles(String marketId, int granularity, Instant start) throws InterruptedException
	{
		return getCandles(marketId, granularity, start, start.plusSeconds(granularity * 60 * 1000));
	}

	public List<APICandle> getCandles(String marketId, int granularity, Instant start, Instant end) throws InterruptedException
	{
		rateLimit();
		Map<String, String> options = new HashMap<String, String>();
		
		options.put("symbol", marketId);
		options.put("startTime", Long.toString(start.getEpochSecond() * 1000));
		options.put("endTime", Long.toString(end.getEpochSecond() * 1000));
		options.put("interval", getIntervalString(granularity));
		options.put("limit", "1000");
		
		Call<List<APICandle>> call = apiInterface.getCandles(options);
		
		Response<List<APICandle>> response;
		try {
			response = call.execute();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String weight = response.headers().get("x-mbx-used-weight-1m");
		if (weight != null)
			usedWeight = Integer.parseInt(weight);
		else
			usedWeight++;
		return response.body();
	}
/*
	@Override
	public List<APICandle> getMonthCandles(String marketId, int granularity, YearMonth month)
		throws InterruptedException
	{
		List<APICandle> candles = new ArrayList<APICandle>();
		Instant startMonth = month.atDay(1).atStartOfDay(ZoneId.of("UTC")).toInstant();
		Instant lastMonth = (month.equals(YearMonth.now())) ? Instant.now() : month.atEndOfMonth().atTime(23, 59, 59, 999999999).atZone(ZoneId.of("UTC")).toInstant();
		Instant start = startMonth;
		while (start.isBefore(lastMonth)) {
			Instant end = start.plusSeconds(granularity * 60 * 1000);
			if (end.isAfter(lastMonth))
				end = lastMonth;
			candles.addAll(getCandles(marketId, granularity, start, end));
			start = end.plusSeconds(1);
		}
		return candles;
	}*/
	
	public List<APICandle> getThousandCandles(String marketId, int granularity, Instant start, boolean forward)
			throws InterruptedException
		{
			List<APICandle> returnCandles = new ArrayList<APICandle>();
			while (candles.size() < 1000) {
				Instant end;
				List<APICandle> curCandles;
				if(forward) {
					end = start.plusSeconds(granularity * 60 * 1000);
					curCandles = getCandles(marketId, granularity, start, end);
					start = end.plusSeconds(1);
				}
				else {
					 end = start.minusSeconds(1);
					 start = start.minusSeconds(granularity * 60 * 1000);
					 curCandles = getCandles(marketId, granularity, start, end);
				}
				
				Collections.reverse(curCandles);
				candles.addAll(curCandles);
				
			}
			
			for(int i = 0; i < 1000; i++) {
				returnCandles.add(i, candles.get(0));
				candles.remove(0);
			}
			
			
			return returnCandles;
		}

}
