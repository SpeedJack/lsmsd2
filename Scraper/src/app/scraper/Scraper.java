package app.scraper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.datamodel.StorablePojoCursor;
import app.datamodel.StorablePojoManager;
import app.datamodel.mongo.DBManager;
import app.datamodel.pojos.DataSource;
import app.scraper.net.BinanceConnector;
import app.scraper.net.CoinbaseConnector;
import app.scraper.net.SourceConnector;

public class Scraper
{

	private static Map<String, Class<? extends SourceConnector>> sourceConnectorMap = Map.ofEntries(
			Map.entry("COINBASE", CoinbaseConnector.class),
			Map.entry("BINANCE", BinanceConnector.class)
		);
	private static List<Worker> workers = new ArrayList<Worker>();
		
	
	public static void main(String[] args)
	{
		setupDBManager();
		setupListener();
		
		while(!Listener.getRunning()) {
			createWorkers();
			for (Worker worker: workers)
				worker.start();
			Listener.addWorkers(workers);
			Listener.setRunning(true);
			Thread.yield();
		}
	}
	
	public static void setupDBManager()
	{
		DBManager.setHostname("127.0.0.1");
		DBManager.setPort(27017);
		DBManager.setUsername("root");
		DBManager.setPassword("rootpass");
		DBManager.setDatabase("mydb");
	}
	
	public static void setupListener() 
	{
		Listener.setScraperAddress("127.0.0.1");
		Listener.addAllowedAddress("127.0.0.1");
		Listener.setPortNumber(99999);
		Listener.setRunning(false);
	}
	
	private static void createWorkers()
	{
		StorablePojoManager<DataSource> manager = new StorablePojoManager<DataSource>(DataSource.class);
		List<DataSource> sources;
		try (StorablePojoCursor<DataSource> cursor = (StorablePojoCursor<DataSource>)manager.find()) {
			sources = cursor.toList();
		}
		
		for (Map.Entry<String, Class<? extends SourceConnector>> sourceConnector: sourceConnectorMap.entrySet())
		{
			DataSource source = null;
			for (DataSource curSource: sources)
				if (curSource.getName().equals(sourceConnector.getKey()))
					source = curSource;
			if (source == null)
				source = new DataSource(sourceConnector.getKey());
			SourceConnector connector;
			try {
				connector = sourceConnector.getValue().getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
			workers.add(new Worker(source, connector));
		}
	}

}
