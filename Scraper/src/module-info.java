module scraper
{

	
	exports app.scraper.data to com.google.gson;
	opens app.scraper.data to com.google.gson;
	requires org.mongodb.bson;
	requires com.google.gson;
	requires org.mongodb.driver.core;
	requires org.mongodb.driver.sync.client;
	requires retrofit2;
	requires retrofit2.converter.gson;
}