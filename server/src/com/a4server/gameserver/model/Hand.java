package com.a4server.gameserver.model;

import com.a4server.gameserver.model.inventory.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * рука в которой игрок держит вещь (inventory item)
 * Created by arksu on 09.09.15.
 */
public class Hand
{
	private static final Logger _log = LoggerFactory.getLogger(Hand.class.getName());

	private final InventoryItem _item;

	private final Player _player;

	private final int _offsetX;
	private final int _offsetY;
	private final int _mx;
	private final int _my;

	public Hand(Player player, InventoryItem item, int offsetX, int offsetY, int mx, int my)
	{
		_player = player;
		_item = item;
		_offsetX = offsetX;
		_offsetY = offsetY;
		_mx = mx;
		_my = my;
	}

	public InventoryItem getItem()
	{
		return _item;
	}

	public int getOffsetX()
	{
		return _offsetX;
	}

	public int getOffsetY()
	{
		return _offsetY;
	}
}
