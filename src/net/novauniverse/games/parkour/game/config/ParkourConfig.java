package net.novauniverse.games.parkour.game.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.JSONArray;
import org.json.JSONObject;

import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule;
import net.zeeraa.novacore.spigot.utils.LocationUtils;
import net.zeeraa.novacore.spigot.utils.VectorArea;

public class ParkourConfig extends MapModule {
	private List<ParkourMap> maps;
	private boolean shuffleOrder;

	public ParkourConfig(JSONObject json) {
		super(json);

		this.maps = new ArrayList<ParkourMap>();

		JSONArray maps = json.getJSONArray("maps");
		for (int i = 0; i < maps.length(); i++) {
			JSONObject map = maps.getJSONObject(i);

			World defaultWorld = Bukkit.getServer().getWorlds().get(0);

			Location spawnLocation = LocationUtils.fromJSONObject(map.getJSONObject("start_location"), defaultWorld);
			Location spectatorLocation = LocationUtils.fromJSONObject(map.getJSONObject("spectator_location"), defaultWorld);

			VectorArea completeArea = VectorArea.fromJSON(map.getJSONObject("complete_area"));

			int health = 20;

			if (map.has("health")) {
				health = map.getInt("health");
			}

			int time = 120;
			if (map.has("time")) {
				time = map.getInt("time");
			}

			boolean nightVision = false;
			if (map.has("night_vision")) {
				nightVision = map.getBoolean("night_vision");
			}

			List<ParkourCheckpoint> checkpoints = new ArrayList<>();
			if (map.has("checkpoints")) {
				JSONArray checkpointsJSON = map.getJSONArray("checkpoints");
				for (int j = 0; j < checkpointsJSON.length(); j++) {
					JSONObject checkpointJSON = checkpointsJSON.getJSONObject(j);

					int cVal = checkpointJSON.getInt("value");
					VectorArea cArea = VectorArea.fromJSON(checkpointJSON.getJSONObject("area"));
					Location cStartLocation = LocationUtils.fromJSONObject(checkpointJSON.getJSONObject("start_location"), Bukkit.getServer().getWorlds().stream().findFirst().get());

					ParkourCheckpoint checkpoint = new ParkourCheckpoint(cArea, cVal, cStartLocation);

					checkpoints.add(checkpoint);
				}
			}

			ParkourMap mapData = new ParkourMap(completeArea, spawnLocation, spectatorLocation, health, time, nightVision, checkpoints);
			this.maps.add(mapData);
		}

		if (json.has("shuffle_map_order")) {
			this.shuffleOrder = json.getBoolean("shuffle_map_order");
		}
	}

	public boolean isShuffleOrder() {
		return shuffleOrder;
	}

	public List<ParkourMap> getMaps() {
		return maps;
	}

	@Override
	public void onGameStart(Game game) {
		this.maps.forEach(map -> map.setupWorld(game.getWorld()));
	}
}