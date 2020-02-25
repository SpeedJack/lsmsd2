package app.scraper.datamodel.mongo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mongodb.client.model.Filters;

public abstract class DataObject
{
	private transient Document updateDocument;
	private transient boolean saved;
	private transient DBManager db;
	
	protected DataObject(boolean saved)
	{
		db = DBManager.getInstance();
		this.saved = saved;
	}
	
	protected DataObject()
	{
		this(false);
	}
	
	public static <T extends DataObject> String getCollectionName(Class<T> objType)
	{
		return objType.isAnnotationPresent(CollectionName.class)
			? objType.getAnnotation(CollectionName.class).value() : objType.getName();
	}
	
	public String getCollectionName()
	{
		return getCollectionName(this.getClass());
	}
	
	public static <T extends DataObject> List<T> load(Class<T> objType)
	{
		return load(objType, null);
	}

	public static <T extends DataObject> List<T> load(Class<T> objType, Bson filter)
	{

		Gson gson = new Gson();
		List<Document> documents = DBManager.getInstance().find(getCollectionName(objType), filter);
		List<T> sources = new ArrayList<T>();
		for (Document document: documents) {
			T source = gson.fromJson(document.toJson(), objType);
			source.postLoad();
			sources.add(source);
		}
		return sources;
	}
	
	protected void postLoad()
	{
		saved = true;
	}
	
	protected Document getUpdateDocument()
	{
		return (saved == false) ? null : updateDocument;
	}
	
	public boolean isSaved()
	{
		return saved;
	}
	
	protected DBManager getDB()
	{
		return db;
	}
	
	public void save()
	{
		if (saved) {
			update();
			return;
		}
		Document document = getCreateDocument();
		db.insert(getCollectionName(), document);
		saved = true;
	}
	
	Document getCreateDocument()
	{
		Document document = new Document();
		for (Field field: this.getClass().getDeclaredFields())
			if (!Modifier.isTransient(field.getModifiers())) {
				String serializedName;
				if (field.isAnnotationPresent(SerializedName.class))
					serializedName = field.getAnnotation(SerializedName.class).value();
				else
					serializedName = field.getName();
				Object value;
				try {
					field.setAccessible(true);
					value = field.get(this);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					throw new UnsupportedOperationException();
				}
				if (!(value instanceof List<?>)) {
					document.append(serializedName, value);
					continue;
				}
				List<?> list = (List<?>) value;
				List<Document> documents = new ArrayList<Document>();
				for (Object obj: list) {
					DataObject dataObj = (DataObject)obj;
					documents.add(dataObj.getCreateDocument());
				}
				document.append(serializedName, documents);
			}
		return document;
	}
	
	public void update()
	{
		if (getUpdateDocument() == null)
			return;
		db.updateOne(getCollectionName(), getIdFilter(), new Document("$set", updateDocument));
		updateDocument = null;
	}
	
	Bson getIdFilter()
	{
		Object value;
		Field idField;
		try {
			idField = this.getClass().getDeclaredField("_id");
		} catch (NoSuchFieldException | SecurityException e) {
			for (Field field: this.getClass().getDeclaredFields())
				if (!Modifier.isTransient(field.getModifiers()) && field.isAnnotationPresent(SerializedName.class) && field.getAnnotation(SerializedName.class).value().equals("_id")) {
					idField = field;
					break;
				}
			throw new UnsupportedOperationException();
		}
		try {
			value = idField.get(this);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException();
		}
		return Filters.eq("_id", value);
	}
	
	protected void updateField(String fieldName, Object value)
	{
		Field field;
		if (value instanceof List<?>)
			throw new IllegalArgumentException();
		try {
			field = this.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException();
		}
		if (Modifier.isTransient(field.getModifiers()))
			return;
		try {
			field.setAccessible(true);
			if (field.get(this).equals(value))
				return;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException();
		}
		if (updateDocument == null)
			updateDocument = new Document();
		String serializedName;
		if (field.isAnnotationPresent(SerializedName.class))
			serializedName = field.getAnnotation(SerializedName.class).value();
		else
			serializedName = fieldName;
		try {
			field.set(this, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		updateDocument.append(serializedName, value);
	}
	
	public void delete()
	{
		db.deleteOne(getCollectionName(), getIdFilter());
	}
	
	public void replace()
	{
		if (!saved) {
			save();
			return;
		}
		db.replaceOne(getCollectionName(), getIdFilter(), getCreateDocument());
	}
	
	protected List<Document> aggregate(String collectionName, List<Bson> stages)
	{
		return db.aggregate(collectionName, stages);
		
	}
}
