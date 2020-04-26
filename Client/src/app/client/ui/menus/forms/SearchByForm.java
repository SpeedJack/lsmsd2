package app.client.ui.menus.forms;

import java.util.ArrayList;
import java.util.List;

public class SearchByForm extends TextForm
{
	protected String searchBy;

	public SearchByForm(String searchBy)
	{
		super("Search by " + searchBy + " (press ENTER to find all entities)");
		this.searchBy = searchBy;
	}

	@Override
	protected List<FormField> createFields()
	{
		List<FormField> fields = new ArrayList<FormField>();
		fields.add(new FormField(searchBy));
		return fields;
	}
}
