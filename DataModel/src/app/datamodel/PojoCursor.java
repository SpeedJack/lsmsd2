package app.datamodel;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.MongoCursor;

public class PojoCursor<T extends Object> implements AutoCloseable
{
	private MongoCursor<T> cursor;

	PojoCursor(MongoCursor<T> cursor)
	{
		this.cursor = cursor;
	}

	PojoCursor(PojoCursor<T> cursor)
	{
		this.cursor = cursor.cursor;
	}

	public T next()
	{
		return cursor.tryNext();
	}

	public boolean hasNext()
	{
		return cursor.hasNext();
	}

	@Override
	public void close()
	{
		cursor.close();
	}

	public List<T> toList()
	{
		List<T> list = new ArrayList<T>();
		for (T pojo = next(); pojo != null; pojo = next())
			list.add(pojo);
		return list;
	}
}
