package app.client.ui.menus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.client.net.Protocol;
import app.client.ui.Console;
import app.client.ui.menus.forms.CrossForm;
import app.client.ui.menus.forms.MarketGranularityForm;
import app.client.ui.menus.forms.SearchForm;
import app.client.ui.menus.forms.StrategyFileForm;
import app.client.ui.menus.forms.choices.CrossChoice;
import app.common.net.ResponseMessage;
import app.common.net.entities.FileContent;
import app.common.net.entities.MarketInfo;
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
		if (strategy.isDeletable())
			menu.add(new MenuEntry(5, "Delete strategy", true, this::handleDeleteStrategy));
		menu.add(new MenuEntry(0, "Go back", true));
		return menu;
	}

	private void handleViewStrategy(MenuEntry entry)
	{
		Console.println("Name: " + strategy.getName());
		Console.println("Author: " + strategy.getAuthor());
		Console.pause();
	}

	private void handleBrowseReports(MenuEntry entry)
	{
		HashMap<String, String> response = new SearchForm("Market Name").show();
		SelectMarketMenu marketMenu = new SelectMarketMenu(response.get("Market Name"));
		marketMenu.show();
		String market = marketMenu.getSelection().getFullId();
		new ReportListMenu((StrategyInfo)entry.getHandlerData(), market).show();
	}

	private void handleRunStrategy(MenuEntry entry)
	{
		HashMap<String, String> response = new SearchForm("Market Name").show();
		SelectMarketMenu marketMenu = new SelectMarketMenu(response.get("Market Name"));
		marketMenu.show();
		MarketInfo market = marketMenu.getSelection();

		response = new CrossForm(market.getBaseCurrency(), market.getQuoteCurrency()).show();
		String inverse = response.get("Inverse Cross");
		response = new MarketGranularityForm(market.getGranularity()).show();
		int granularity = Integer.parseUnsignedInt(response.get("Granularity"));

		// TODO: ask for additional parameters

		ResponseMessage resMsg = Protocol.getInstance().runStrategy(strategy.getName(), market.getFullId(), CrossChoice.valueOf(inverse).isInverseCross(), granularity);
		if (!resMsg.isSuccess()) {
			Console.println(resMsg.getErrorMsg());
			return;
		}
		Console.println("Strategy correctly run.");

		//TODO: temporarily commented out: the server actually responds with an empty msg
		//ReportInfo report = resMsg.getEntity(ReportInfo.class);
	}

	private void handleDeleteStrategy(MenuEntry entry)
	{
		ResponseMessage resMsg = Protocol.getInstance().deleteStrategy(strategy.getName());
		if (!resMsg.isSuccess()) {
			Console.println(resMsg.getErrorMsg());
			return;
		}
		Console.println("Strategy correctly deleted.");
	}

	private void handleDownloadStrategy(MenuEntry entry)
	{
		HashMap<String, String> response = new StrategyFileForm().show();
		ResponseMessage resMsg = Protocol.getInstance().downloadStrategy(strategy.getName());
		if (!resMsg.isSuccess()) {
			Console.println(resMsg.getErrorMsg());
			return;
		}
		FileContent file = resMsg.getEntity(FileContent.class);
		try {
			file.writeFile(response.get("File"));
			Console.println("Strategy successfully downloaded.");
		} catch (IOException e) {
			Console.println("Can not write file '" + response.get("Path") + "': " + e.getMessage());
		}
	}
}
