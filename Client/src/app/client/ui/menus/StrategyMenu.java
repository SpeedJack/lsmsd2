package app.client.ui.menus;

import java.util.ArrayList;
import java.util.List;

import app.client.net.Protocol;
import app.client.ui.Console;
import app.common.net.entities.StrategyInfo;


public class StrategyMenu extends Menu
{
	protected StrategyInfo strategy;

	public StrategyMenu(StrategyInfo strategy)
	{
		super(strategy.getName() + " | Select an action");
		this.strategy = strategy;
	}

	@Override
	protected List<MenuEntry> getMenu()
	{
		List<MenuEntry> menu = new ArrayList<MenuEntry>();
		menu.add(new MenuEntry(1, "View details", this::handleViewStrategy));
		menu.add(new MenuEntry(2, "Browse reports", this::handleBrowseReports));
		menu.add(new MenuEntry(3, "Run strategy", this::handleRunStrategy));
		menu.add(new MenuEntry(4, "Download strategy", this::handleDownloadStrategy));
		if(strategy.isDeletable()) {
			menu.add(new MenuEntry(5, "Delete strategy", true, this::handleDeleteStrategy));
		}
		menu.add(new MenuEntry(0, "Go back", true));
		return menu;
	}

	private void handleViewStrategy(MenuEntry entry)
	{
		Console.println("Name: " + strategy.getName());
		Console.println("Author: " + strategy.getUsername());
		Console.pause();
	}

	private void handleBrowseReports(MenuEntry entry)
	{
		/*HashMap<String, String> response = new UserForm("Market Name").show();
		new ReportListMenu((StrategyInfo)entry.getHandlerData(), response.get("")).show();*/
		//TODO: nope, we should select the market from a list
	}

	private void handleRunStrategy(MenuEntry entry)
	{
		//TODO
	}

	private void handleDeleteStrategy(MenuEntry entry)
	{
		Protocol.getInstance().deleteStrategy(strategy.getName());
	}

	private void handleDownloadStrategy(MenuEntry entry)
	{
		//TODO
	}
}
