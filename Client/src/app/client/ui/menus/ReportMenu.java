package app.client.ui.menus;

import java.util.ArrayList;
import java.util.List;

import app.client.net.Protocol;
import app.client.ui.Console;
import app.client.ui.menus.forms.AmountForm;
import app.common.net.ResponseMessage;
import app.common.net.entities.ReportInfo;

public class ReportMenu extends Menu
{
	protected ReportInfo report;

	public ReportMenu(ReportInfo report)
	{
		super(report.getStrategyName() + ": " + report.getStart() + " - " + report.getEnd() + " on " + report.getMarket() + " by " + report.getAuthor() + " | Select an action");
		this.report = report;
	}

	@Override
	protected List<MenuEntry> getMenu()
	{
		List<MenuEntry> menu = new ArrayList<MenuEntry>();
		menu.add(new MenuEntry(1, "View report", this::handleViewReport));
		if(report.isDeletable())
			menu.add(new MenuEntry(2, "Delete report", true, this::handleDeleteReport));
		menu.add(new MenuEntry(0, "Go back", true));
		return menu;
	}

	private void handleViewReport(MenuEntry entry)
	{
		int amount = Integer.parseInt(new AmountForm().show().get("Amount"));

		Console.println("Id: " + report.getRunId());
		Console.println("Strategy: " + report.getStrategyName());
		Console.println("Start: " + report.getStart());
		Console.println("End: " + report.getEnd());
		//TODO: other report parameters
		Console.println("Author: " + report.getAuthor());
		Console.println("Net Profit: " + report.getNetProfit()*amount);
		Console.println("Gross Profit: " + report.getGrossProfit()*amount);
		Console.println("Gross Loss: "  + report.getGrossLoss()*amount);
		Console.println("Hodl Profit: " + report.getHodlProfit()*amount);
		Console.println("Total Trades: " + report.getTotalTrades());
		Console.println("Open Trades: " + report.getOpenTrades());
		Console.println("Winning Trades: " + report.getWinningTrades());
		Console.println("Max Consecutive Loosing: " + report.getMaxConsecutiveLosing());
		Console.println(" Average Amount: " + report.getAvgAmount()*amount);
		Console.println("Average Duration: " + report.getAvgDuration());
		Console.println("Max Drawdown: " + report.getMaxDrawdown()*amount);
		Console.pause();
	}

	private void handleDeleteReport(MenuEntry entry)
	{
		if (!Console.askConfirm()) {
			Console.println("Aborting...");
			return;
		}
		ResponseMessage resMsg = Protocol.getInstance().deleteReport(report.getRunId());
		if(!resMsg.isSuccess()) {
			Console.println(resMsg.getErrorMsg());
			return;
		}
		Console.println("Report successfully deleted!");
	}
}
