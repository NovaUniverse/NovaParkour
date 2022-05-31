package net.novauniverse.games.parkour.game.config;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import net.zeeraa.novacore.spigot.utils.VectorArea;

public class ParkourMap {
	private VectorArea completeArea;

	private Location spawnLocation;
	private Location spectatorLocation;

	private int health;
	private int time;

	private boolean nightVision;

	private List<ParkourCheckpoint> checkpoints;

	public ParkourMap(VectorArea completeArea, Location spawnLocation, Location spectatorLocation, int health, int time, boolean nightVision, List<ParkourCheckpoint> checkpoints) {
		this.completeArea = completeArea;
		this.spawnLocation = spawnLocation;
		this.spectatorLocation = spectatorLocation;
		this.health = health;
		this.time = time;
		this.nightVision = nightVision;
		this.checkpoints = checkpoints;
	}

	public void setupWorld(World world) {
		this.spawnLocation = new Location(world, this.spawnLocation.getX(), this.spawnLocation.getY(), this.spawnLocation.getZ(), this.spawnLocation.getYaw(), this.spawnLocation.getPitch());
		this.spectatorLocation = new Location(world, this.spectatorLocation.getX(), this.spectatorLocation.getY(), this.spectatorLocation.getZ(), this.spectatorLocation.getYaw(), this.spectatorLocation.getPitch());
		this.checkpoints.forEach(checkpoint -> checkpoint.setupWorld(world));
	}

	public VectorArea getCompleteArea() {
		return completeArea;
	}

	public Location getSpawnLocation() {
		return spawnLocation;
	}

	public Location getSpectatorLocation() {
		return spectatorLocation;
	}

	public int getHealth() {
		return health;
	}

	public int getTime() {
		return time;
	}

	public boolean isNightVision() {
		return nightVision;
	}

	public List<ParkourCheckpoint> getCheckpoints() {
		return checkpoints;
	}
}