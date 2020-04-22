package app.client.ui.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import app.client.net.Protocol;
import app.client.ui.Console;
import app.common.net.ResponseMessage;
import app.common.net.entities.Entity;
import app.common.net.entities.SourceInfo;

public class DataSourceListMenu extends Menu {
	List<SourceInfo> sources = new ArrayList<SourceInfo>();

	DataSourceListMenu()
	{
		super("This is the list of all available Data Sources");
		
	}
	@Override
	protected SortedSet<MenuEntry> getMenu()
	{
		ResponseMessage resMsg = Protocol.getInstance().browseDataSource();
		
		if(!resMsg.isSuccess()) {
			Console.println(resMsg.getErrorMsg());
			return null;
		}
		sources.clear();
		for(Entity entity : resMsg.getEntities())
			this.sources.add((SourceInfo)entity);
		
		SortedSet<MenuEntry> menu = new TreeSet<>();
		int i = 1;
		for(SourceInfo source : sources) {
			menu.add(new MenuEntry(i, source.getName(), this::handleDataSourceSelection, source));
			++i;
		}
		
		menu.add(new MenuEntry(0, "Go back", true));
		return menu;
	}
	
	void handleDataSourceSelection(MenuEntry entry)
	{
		new DataSourceMenu((SourceInfo)entry.getHandlerData()).show();;
		
	}

}
