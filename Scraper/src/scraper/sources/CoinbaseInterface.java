package scraper.sources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import scraper.db.Candle;


public interface CoinbaseInterface {
	
	@GET("products")
	Call<List<CoinbaseMarket>> listMarkets();
	@GET("products/{market}/candles")
	Call<ArrayList<Candle>> getBars(@Path("market") String id, @QueryMap Map<String, Long> options);
	
}
