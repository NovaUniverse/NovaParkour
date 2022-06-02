package net.novauniverse.games.parkour.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import net.novauniverse.games.parkour.game.config.TeleporterConfig;

public class ParkourPlayerReachTeleporterEvent extends ParkourEvent {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private TeleporterConfig teleporter;
	private Player player;

	public ParkourPlayerReachTeleporterEvent(TeleporterConfig teleporter, Player player) {
		this.teleporter = teleporter;
		this.player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	public TeleporterConfig getTeleporter() {
		return teleporter;
	}

	public Player getPlayer() {
		return player;
	}
}