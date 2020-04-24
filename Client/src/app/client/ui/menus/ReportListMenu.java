	package app.client.ui.menus;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
	import java.util.TreeSet;

import app.client.net.Protocol;
import app.client.ui.Console;
	import app.client.ui.menus.MenuEntry;
import app.client.ui.menus.forms.SearchAmountForm;
import app.common.net.ResponseMessage;
import app.common.net.entities.BrowseInfo;
import app.common.net.entities.Entity;
import app.common.net.entities.ReportInfo;
import app.common.net.entities.StrategyInfo;

public class ReportListMenu extends Menu {
	
	protected StrategyInfo strategy;
	protected List<ReportInfo> reports = new ArrayList<ReportInfo>();
	protected String filter;
	protected int currentPage;
	
	public ReportListMenu(StrategyInfo strategy, String filter)
	{
		super("Selected reports of this strategy");
		this.strategy = strategy;
		this.filter = filter;
		this.currentPage = 1;
	}

	@Override
	protected SortedSet<MenuEntry> getMenu()
	{
		HashMap<String, String> filters = new HashMap<String,String>();
		filters.put("strategy", strategy.getName());
		filters.put("filter", filter);
		ResponseMessage resMsg = Protocol.getInstance().browseReports(new BrowseInfo(filters, currentPage));
		
		if(!resMsg.isSuccess()) {
			Console.println(resMsg.getErrorMsg());
			return null;
		}
		
		reports.clear();
		for(Entity entity: resMsg.getEntities()) {
			reports.add((ReportInfo)entity);
		}

		SortedSet<MenuEntry> menu = new TreeSet<>();
		int i=1;
		for(ReportInfo report : reports) {
			if(report.isCanDelete()) {
				menu.add(new MenuEntry(i, "Market Name: " + report.getMarketname() + ", " 
						+ "Time Range: " + report.getStart() + "-" + report.getEnd() + ", "
						+ "you are the author!", this::handleReportSelection, report));
			}
			else {
				menu.add(new MenuEntry(i, "Market Name: " + report.getMarketname() + ", " 
						+ "Time Range: " + report.getStart() + "-" + report.getEnd() + ", "
						+ report.getAuthor(), this::handleReportSelection, report));
			}
		}
				
		menu.add(new MenuEntry(1, "Load a new page", this::handleLoadNewPage));
		menu.add(new MenuEntry(0, "Go back", true));
		return menu;
	}

	private void handleReportSelection(MenuEntry entry)
	{
		HashMap<Integer, String> response = new SearchAmountForm("Amount: ").show();
		new ReportMenu((ReportInfo)entry.getHandlerData(), Double.parseDouble(response.get(0)));

	}
	
	private void handleLoadNewPage(MenuEntry entry) 
	{
		currentPage++;
		getMenu();
		
	}
}
