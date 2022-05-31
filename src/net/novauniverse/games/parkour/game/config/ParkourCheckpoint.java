package net.novauniverse.games.parkour.game.config;

import org.bukkit.Location;
import org.bukkit.World;

import net.zeeraa.novacore.spigot.utils.VectorArea;

public class ParkourCheckpoint {
	private VectorArea unlockArea;
	private int value;
	private Location spawnLocation;

	public ParkourCheckpoint(VectorArea unlockArea, int value, Location spawnLocation) {
		this.unlockArea = unlockArea;
		this.value = value;
		this.spawnLocation = spawnLocation;
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
}