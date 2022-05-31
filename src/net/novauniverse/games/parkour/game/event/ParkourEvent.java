package net.novauniverse.games.parkour.game.event;

import org.bukkit.event.Event;

import net.novauniverse.games.parkour.NovaParkour;
import net.novauniverse.games.parkour.game.Parkour;

public abstract class ParkourEvent extends Event {
	public Parkour getGame() {
		return (Parkour) NovaParkour.getInstance().getGame();
	}
}