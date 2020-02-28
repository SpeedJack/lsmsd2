package app.client.ui.menus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import app.client.ui.Console;
import app.client.ui.menus.MenuEntry;

public class UserListMenu extends Menu
{

	public UserListMenu()
	{
		super("All the available strategies");
	}

	@Override
	protected SortedSet<MenuEntry> getMenu()
	{
		//richiedere una pagina di strategie al server
		//gestire casi di errore o se non ci sono strategie

		SortedSet<MenuEntry> menu = new TreeSet<>();
		
		//per ogni user i trovato ...
		menu.add(new MenuEntry(i, user.getName() + user.isAdmin().toString, true, this::handleDeleteUser, user));
				
		menu.add(new MenuEntry(1, "Load a new page", this::handleLoadNewPage));
		menu.add(new MenuEntry(0, "Go back", true));
		return menu;
	}

	private void handleDeleteUser(MenuEntry entry)
	{
		boolean confirm = Console.askConfirm("This will remove this user. Are you sure?");
		if (confirm) {
			//mandare la server la cancellazione della strategia
		}
		Console.newLine();
		
	}
	
	private void handleLoadNewPage(MenuEntry entry) 
	{
		currentPage++;
		
	}
}
