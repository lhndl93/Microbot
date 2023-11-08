/*
 * Copyright (c) 2018, Charlie Waters
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.itemprices;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;

class ItemPricesOverlay extends Overlay
{
	private static final int INVENTORY_ITEM_WIDGETID = ComponentID.INVENTORY_CONTAINER;
	private static final int BANK_INVENTORY_ITEM_WIDGETID = ComponentID.BANK_INVENTORY_ITEM_CONTAINER;
	private static final int BANK_ITEM_WIDGETID = ComponentID.BANK_ITEM_CONTAINER;
	private static final int EXPLORERS_RING_ITEM_WIDGETID = ComponentID.EXPLORERS_RING_INVENTORY;
	private static final int SEED_VAULT_ITEM_WIDGETID = ComponentID.SEED_VAULT_ITEM_CONTAINER;
	private static final int SEED_VAULT_INVENTORY_ITEM_WIDGETID = ComponentID.SEED_VAULT_INVENTORY_ITEM_CONTAINER;
	private static final int POH_TREASURE_CHEST_INVENTORY_ITEM_WIDGETID = ComponentID.POH_TREASURE_CHEST_INV_CONTAINER;

	private final Client client;
	private final ItemPricesConfig config;
	private final TooltipManager tooltipManager;
	private final StringBuilder itemStringBuilder = new StringBuilder();

	@Inject
	ItemManager itemManager;

	@Inject
	ItemPricesOverlay(Client client, ItemPricesConfig config, TooltipManager tooltipManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.config = config;
		this.tooltipManager = tooltipManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.isMenuOpen())
		{
			return null;
		}

		final MenuEntry[] menuEntries = client.getMenuEntries();
		final int last = menuEntries.length - 1;

		if (last < 0)
		{
			return null;
		}

		final MenuEntry menuEntry = menuEntries[last];
		final MenuAction action = menuEntry.getType();
		final int widgetId = menuEntry.getParam1();
		final int groupId = WidgetUtil.componentToInterface(widgetId);
		final boolean isAlching = menuEntry.getOption().equals("Cast") && menuEntry.getTarget().contains("High Level Alchemy");

		// Tooltip action type handling
		switch (action)
		{
			case WIDGET_TARGET_ON_WIDGET:
				// Check target widget is the inventory
				if (menuEntry.getWidget().getId() != ComponentID.INVENTORY_CONTAINER)
				{
					break;
				}
				// FALLTHROUGH
			case WIDGET_USE_ON_ITEM:
				// Require showWhileAlching and Cast High Level Alchemy
				if (!config.showWhileAlching() || !isAlching)
				{
					break;
				}
				// FALLTHROUGH
			case CC_OP:
			case ITEM_USE:
			case ITEM_FIRST_OPTION:
			case ITEM_SECOND_OPTION:
			case ITEM_THIRD_OPTION:
			case ITEM_FOURTH_OPTION:
			case ITEM_FIFTH_OPTION:
				addTooltip(menuEntry, isAlching, groupId);
				break;
			case WIDGET_TARGET:
				// Check that this is the inventory
				if (menuEntry.getWidget().getId() == ComponentID.INVENTORY_CONTAINER)
				{
					addTooltip(menuEntry, isAlching, groupId);
				}
		}

		return null;
	}

	private void addTooltip(MenuEntry menuEntry, boolean isAlching, int groupId)
	{
		// Item tooltip values
		switch (groupId)
		{
			case InterfaceID.EXPLORERS_RING:
				if (!config.showWhileAlching())
				{
					return;
				}
			case InterfaceID.INVENTORY:
			case InterfaceID.POH_TREASURE_CHEST_INV:
				if (config.hideInventory() && (!config.showWhileAlching() || !isAlching))
				{
					return;
				}
				// FALLTHROUGH
			case InterfaceID.BANK:
			case InterfaceID.BANK_INVENTORY:
			case InterfaceID.SEED_VAULT:
			case InterfaceID.SEED_VAULT_INVENTORY:
				// Make tooltip
				final String text = makeValueTooltip(menuEntry);
				if (text != null)
				{
					tooltipManager.add(new Tooltip(ColorUtil.prependColorTag(text, new Color(238, 238, 238))));
				}
		}
	}

	private String makeValueTooltip(MenuEntry menuEntry)
	{
		// Disabling both disables all value tooltips
		if (!config.showGEPrice() && !config.showHAValue())
		{
			return null;
		}

		final int widgetId = menuEntry.getParam1();
		ItemContainer container = null;

		// Inventory item
		if (widgetId == INVENTORY_ITEM_WIDGETID ||
			widgetId == BANK_INVENTORY_ITEM_WIDGETID ||
			widgetId == EXPLORERS_RING_ITEM_WIDGETID ||
			widgetId == SEED_VAULT_INVENTORY_ITEM_WIDGETID ||
			widgetId == POH_TREASURE_CHEST_INVENTORY_ITEM_WIDGETID)
		{
			container = client.getItemContainer(InventoryID.INVENTORY);
		}
		// Bank item
		else if (widgetId == BANK_ITEM_WIDGETID)
		{
			container = client.getItemContainer(InventoryID.BANK);
		}
		// Seed vault item
		else if (widgetId == SEED_VAULT_ITEM_WIDGETID)
		{
			container = client.getItemContainer(InventoryID.SEED_VAULT);
		}

		if (container == null)
		{
			return null;
		}

		// Find the item in the container to get stack size
		final int index = menuEntry.getParam0();
		final Item item = container.getItem(index);
		if (item != null)
		{
			return getItemStackValueText(item);
		}

		return null;
	}

	private String getItemStackValueText(Item item)
	{
		int id = itemManager.canonicalize(item.getId());
		int qty = item.getQuantity();

		// Special case for coins and platinum tokens
		if (id == ItemID.COINS_995)
		{
			return QuantityFormatter.formatNumber(qty) + " gp";
		}
		else if (id == ItemID.PLATINUM_TOKEN)
		{
			return QuantityFormatter.formatNumber(qty * 1000L) + " gp";
		}

		ItemComposition itemDef = itemManager.getItemComposition(id);

		// Only check prices for things with store prices
		if (itemDef.getPrice() <= 0)
		{
			return null;
		}

		int gePrice = 0;
		int haPrice = 0;
		int haProfit = 0;
		final int itemHaPrice = itemDef.getHaPrice();

		if (config.showGEPrice())
		{
			gePrice = itemManager.getItemPrice(id);
		}
		if (config.showHAValue())
		{
			haPrice = itemHaPrice;
		}
		if (gePrice > 0 && itemHaPrice > 0 && config.showAlchProfit())
		{
			haProfit = calculateHAProfit(itemHaPrice, gePrice);
		}

		if (gePrice > 0 || haPrice > 0)
		{
			return stackValueText(qty, gePrice, haPrice, haProfit);
		}

		return null;
	}

	private String stackValueText(int qty, int gePrice, int haValue, int haProfit)
	{
		if (gePrice > 0)
		{
			itemStringBuilder.append("GE: ")
				.append(QuantityFormatter.quantityToStackSize((long) gePrice * qty))
				.append(" gp");
			if (config.showEA() && qty > 1)
			{
				itemStringBuilder.append(" (")
					.append(QuantityFormatter.quantityToStackSize(gePrice))
					.append(" ea)");
			}
		}
		if (haValue > 0)
		{
			if (gePrice > 0)
			{
				itemStringBuilder.append("</br>");
			}

			itemStringBuilder.append("HA: ")
				.append(QuantityFormatter.quantityToStackSize((long) haValue * qty))
				.append(" gp");
			if (config.showEA() && qty > 1)
			{
				itemStringBuilder.append(" (")
					.append(QuantityFormatter.quantityToStackSize(haValue))
					.append(" ea)");
			}
		}

		if (haProfit != 0)
		{
			Color haColor = haProfitColor(haProfit);

			itemStringBuilder.append("</br>");
			itemStringBuilder.append("HA Profit: ")
				.append(ColorUtil.wrapWithColorTag(String.valueOf((long) haProfit * qty), haColor))
				.append(" gp");
			if (config.showEA() && qty > 1)
			{
				itemStringBuilder.append(" (")
					.append(ColorUtil.wrapWithColorTag(String.valueOf(haProfit), haColor))
					.append(" ea)");
			}
		}

		// Build string and reset builder
		final String text = itemStringBuilder.toString();
		itemStringBuilder.setLength(0);
		return text;
	}

	private int calculateHAProfit(int haPrice, int gePrice)
	{
		int natureRunePrice = itemManager.getItemPrice(ItemID.NATURE_RUNE);
		return haPrice - gePrice - natureRunePrice;
	}

	private static Color haProfitColor(int haProfit)
	{
		return haProfit >= 0 ? Color.GREEN : Color.RED;
	}
}
