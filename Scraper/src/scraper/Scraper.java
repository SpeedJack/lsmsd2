package scraper;

import java.util.ArrayList;

import scraper.sources.Coinbase;
import scraper.sources.DataSource;
import scraper.sources.Market;

public class Scraper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataSource ds = new Coinbase();
		ArrayList<Market> m = ds.getMarkets();
		for(Market market: m)
				System.out.println(market.id);

	}

}
