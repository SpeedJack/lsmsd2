package app.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;

import app.datamodel.mongo.DBManager;
import app.datamodel.pojos.DataSource;
import app.datamodel.pojos.Market;
import app.datamodel.pojos.PojoState;

public class SourcesManager {
	
	protected static MongoDatabase getDB()
	{
		return DBManager.getInstance().getDatabase();
	}

	protected MongoCollection<DataSource> getCollection()
	{
		return getDB().getCollection("Sources", DataSource.class);
	}
	
	public void insert(DataSource dataSource)
	{
		getCollection().insertOne(dataSource);
	}
	
	public void insert(List<DataSource> dataSources) 
	{
		getCollection().insertMany(dataSources);
	}
	
	public boolean delete(DataSource dataSource)
	{
		return getCollection().deleteOne(Filters.eq("_id", dataSource.getName())).wasAcknowledged();
	}
	
	public long delete(List<DataSource> dataSources) 
	{
		long result = 0;
		for(DataSource dataSource : dataSources)
			if(delete(dataSource))
				result++;
		return result;		
	}
	
	public boolean update(DataSource dataSource) 
	{
		
		Bson updateDocument = new Document();
		UpdateOptions options = new UpdateOptions();
		List<Bson> arrayFilters = new ArrayList<Bson>();
		List<Market> addedMarkets = new ArrayList<Market>();
		List<Market> removedMarkets = new ArrayList<Market>();
		
		int filterNumber = 0;
		
		HashMap<String, Object> updatedFields = dataSource.getUpdatedFields();
		for(Map.Entry<String, Object> entry : updatedFields.entrySet())
			updateDocument =Updates.combine(updateDocument, Updates.set(entry.getKey(), entry.getValue()));
		ListIterator<Market> iterator = dataSource.getMarketsIterator();
		
		while(iterator.hasNext()) {
			Market market = iterator.next();
			switch(market.getState()) 
			{
			case STAGED:
				addedMarkets.add(market);
				market.setState(PojoState.COMMITTED);
				continue;
			case REMOVED:
				removedMarkets.add(market);
				iterator.remove();
				continue;
			default:
			}
			
			updatedFields = market.getUpdatedFields();
			if(updatedFields.isEmpty()) 
				continue;
			String filterName = "f" + filterNumber;
			arrayFilters.add(Filters.eq(filterName + ".id", market.getId()));
			for(Map.Entry<String, Object> entry : updatedFields.entrySet()) {
				updateDocument = Updates.combine(updateDocument, Updates.set("markets.$["+filterName+"]."+entry.getKey(), entry.getValue()));
			}
			filterNumber++;
		}
		
		if(!addedMarkets.isEmpty())
			updateDocument = Updates.combine(updateDocument, Updates.pushEach("markets", addedMarkets));
		if(!removedMarkets.isEmpty())
			updateDocument = Updates.combine(updateDocument, Updates.pullAll("markets", removedMarkets));			
		
		//AGGIUNGERE GESTIONE RIMOZIONE MARKETDATA
		
		options.arrayFilters(arrayFilters);		
		return getCollection().updateOne(Filters.eq("_id", dataSource.getName()), updateDocument, options).wasAcknowledged();
	}
	
	public long update(List<DataSource> dataSources) 
	{
		for(DataSource dataSource : dataSources)
			update(dataSource);
		return 0;
	}
	
	//SE SERVE
	//public boolean updateMarket(Market market) {return true;}
	
	
	public void drop() 
	{
		getCollection().drop();
	}
	
	public PojoCursor<DataSource> find(boolean getMarket) //Se false, non si prende i mercati
	{	
		if(!getMarket) {
			FindIterable<DataSource> cursor = getCollection().find().projection(Projections.exclude("markets"));
			return new PojoCursor<DataSource>(cursor.cursor());
		}
		
		FindIterable<DataSource> cursor = getCollection().find();
		return new PojoCursor<DataSource>(cursor.cursor());
	}
	
	public PojoCursor<DataSource> find()
	{
		return find(true);
	}
	
	//SE SERVE
	//public Market findMarket(String id) {return null;}
	//public boolean deleteMarket(Market market) {}
	//public long deleteMarkets(List<Market> markets) {}	
}
