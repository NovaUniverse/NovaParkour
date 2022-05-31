package net.novauniverse.games.parkour.game.event;

import org.bukkit.event.HandlerList;

public class ParkourRoundEndEvent extends ParkourEvent {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}