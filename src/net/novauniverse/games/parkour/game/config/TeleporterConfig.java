package net.novauniverse.games.parkour.game.config;

import org.bukkit.Location;
import org.bukkit.World;

import net.zeeraa.novacore.spigot.utils.VectorArea;

public class TeleporterConfig {
	private VectorArea activationArea;
	private Location targetLocation;
	
	public TeleporterConfig(VectorArea activationArea, Location targetLocation) {
		this.activationArea = activationArea;
		this.targetLocation = targetLocation;
	}

	public void setupWorld(World world) {
		this.targetLocation = new Location(world, this.targetLocation.getX(), this.targetLocation.getY(), this.targetLocation.getZ(), this.targetLocation.getYaw(), this.targetLocation.getPitch());
	}
	
	public VectorArea getActivationArea() {
		return activationArea;
	}
	
	public Location getTargetLocation() {
		return targetLocation;
	}
}