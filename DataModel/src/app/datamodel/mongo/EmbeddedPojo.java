package app.datamodel.mongo;

import java.lang.reflect.Field;
import java.util.Map;

import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.conversions.Bson;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public abstract class EmbeddedPojo extends Pojo
{
	private transient Pojo container;
	private transient boolean embeddedList;


	@BsonIgnore
	@SuppressWarnings("unchecked")
	protected <T extends Pojo> T getContainer()
	{
		return (T)container;
	}
	
	@BsonIgnore
	@Override
	public boolean isSaved() 
	{
		Pojo container = getContainer();
		if(container == null)
			return false;
		return container.isSaved();
	}

	@BsonIgnore
	public void setContainer(Pojo container)
	{
		this.container = container;
	}
	

	@BsonIgnore
	protected void setEmbeddedList(boolean value)
	{
		this.embeddedList = value;
	}
	
	@BsonIgnore
	protected void setEmbeddedList()
	{
		setEmbeddedList(true);
	}
	
	@BsonIgnore
	public boolean isEmbeddedList()
	{
		return embeddedList;
	}

	@BsonIgnore
	public static String getFieldName(Class<? extends EmbeddedPojo> objType)
	{
		if (!objType.isAnnotationPresent(Embedded.class))
			return objType.getName();
		String nestedName = objType.getAnnotation(Embedded.class).nestedName();
		if (nestedName.isEmpty())
			return objType.getName();
		return nestedName;
	}
	
	@BsonIgnore
	public static Class<? extends Pojo> getContainerClass(Class<? extends EmbeddedPojo> objType)
	{
		return objType.isAnnotationPresent(Embedded.class)
			? objType.getAnnotation(Embedded.class).value() : Pojo.class;
	}
	
	@BsonIgnore
	public Class<? extends Pojo> getContainerClass()
	{
		if (container != null)
			return container.getClass();
		return getContainerClass(this.getClass());
	}
	
	@BsonIgnore
	public Bson getContainerFilter()
	{
		return container.getFilter();
	}
	
	@BsonIgnore
	public String getFieldName()
	{
		return getFieldName(this.getClass());
	}
	
	@BsonIgnore
	public Bson getFilter() 
	{
		
		for (Field field: this.getClass().getDeclaredFields())
			if (field.isAnnotationPresent(EmbeddedId.class)) {
				field.setAccessible(true);
				try {
					Bson filter = Filters.eq(this.getClass().getAnnotation(Embedded.class).nestedName() + "." + field.getName(), field.get(this));
					return Filters.and(getContainerFilter(), filter);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					continue;
				}
			}
		throw new UnsupportedOperationException("Class " + this.getClass().getCanonicalName() + " does not specify a BsonId.");
		
	}
	
	@BsonIgnore
	protected Bson getUpdateDocument()
	{
		if(updatedFields.size() == 0) return null;
		Bson document = new Document();
		for (Map.Entry<String, Object> field: updatedFields.entrySet())
			document = Updates.combine(document, Updates.set(getFieldName()+ ".$." + field.getKey(), field.getValue()));
		updatedFields.clear();
		return document;

	}
}
