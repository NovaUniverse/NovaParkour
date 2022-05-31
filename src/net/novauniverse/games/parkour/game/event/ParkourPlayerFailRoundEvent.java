package net.novauniverse.games.parkour.game.event;

import java.util.UUID;

import org.bukkit.event.HandlerList;

public class ParkourPlayerFailRoundEvent extends ParkourEvent {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private UUID uuid;

	public ParkourPlayerFailRoundEvent(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	public UUID getUuid() {
		return uuid;
	}
}