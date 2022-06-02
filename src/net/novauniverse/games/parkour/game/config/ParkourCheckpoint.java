package net.novauniverse.games.parkour.game.config;

import org.bukkit.Location;
import org.bukkit.World;

import net.zeeraa.novacore.spigot.utils.VectorArea;

public class ParkourCheckpoint {
	private VectorArea unlockArea;
	private int value;
	private Location spawnLocation;
	private boolean noMessage;
	private String message;
	private String title;

	public ParkourCheckpoint(VectorArea unlockArea, int value, Location spawnLocation, boolean noMessage, String message, String title) {
		this.unlockArea = unlockArea;
		this.value = value;
		this.spawnLocation = spawnLocation;
		this.noMessage = noMessage;
		this.message = message;
		this.title = title;
	}

	public void setupWorld(World world) {
		this.spawnLocation = new Location(world, this.spawnLocation.getX(), this.spawnLocation.getY(), this.spawnLocation.getZ(), this.spawnLocation.getYaw(), this.spawnLocation.getPitch());
	}

	public Location getSpawnLocation() {
		return spawnLocation;
	}

	public VectorArea getUnlockArea() {
		return unlockArea;
	}

	public int getValue() {
		return value;
	}

	public boolean isNoMessage() {
		return noMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getTitle() {
		return title;
	}
}