package net.novauniverse.games.parkour.game.event;

import java.util.UUID;

import org.bukkit.event.HandlerList;

import net.novauniverse.games.parkour.game.PlacementType;

public class ParkourPlacementEvent extends ParkourEvent {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private UUID uuid;
	private PlacementType type;
	private int placement;
	private int score;

	public ParkourPlacementEvent(UUID uuid, PlacementType type, int placement, int score) {
		this.uuid = uuid;
		this.type = type;
		this.placement = placement;
		this.score = score;
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

	public PlacementType getType() {
		return type;
	}

	public int getPlacement() {
		return placement;
	}

	public int getScore() {
		return score;
	}
}