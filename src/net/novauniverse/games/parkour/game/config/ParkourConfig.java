package net.novauniverse.games.parkour.game.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.JSONArray;
import org.json.JSONObject;

import net.md_5.bungee.api.ChatColor;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule;
import net.zeeraa.novacore.spigot.utils.LocationUtils;
import net.zeeraa.novacore.spigot.utils.VectorArea;

public class ParkourConfig extends MapModule {
	private List<ParkourMap> maps;
	private boolean shuffleOrder;

	private String customGameOverMessage;

	public ParkourConfig(JSONObject json) {
		super(json);

		this.maps = new ArrayList<ParkourMap>();

		this.customGameOverMessage = null;

		if (json.has("game_over_message")) {
			customGameOverMessage = ChatColor.translateAlternateColorCodes('&', json.getString("game_over_message"));
		}

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

					boolean noMessage = false;
					if (checkpointJSON.has("no_message")) {
						noMessage = checkpointJSON.getBoolean("no_message");
					}

					String message = null;
					String title = null;

					if (checkpointJSON.has("message")) {
						message = ChatColor.translateAlternateColorCodes('&', checkpointJSON.getString("message"));
					}

					if (checkpointJSON.has("title")) {
						title = ChatColor.translateAlternateColorCodes('&', checkpointJSON.getString("title"));
					}

					ParkourCheckpoint checkpoint = new ParkourCheckpoint(cArea, cVal, cStartLocation, noMessage, message, title);

					checkpoints.add(checkpoint);
				}
			}

			List<TeleporterConfig> teleporters = new ArrayList<>();
			if (map.has("teleporters")) {
				JSONArray teleportersJSON = map.getJSONArray("teleporters");
				for (int j = 0; j < teleportersJSON.length(); j++) {
					JSONObject teleporterJSON = teleportersJSON.getJSONObject(j);
					VectorArea tArea = VectorArea.fromJSON(teleporterJSON.getJSONObject("activation_area"));
					Location tStartLocation = LocationUtils.fromJSONObject(teleporterJSON.getJSONObject("target_location"), Bukkit.getServer().getWorlds().stream().findFirst().get());

					TeleporterConfig teleporter = new TeleporterConfig(tArea, tStartLocation);

					teleporters.add(teleporter);
				}
			}

			ParkourMap mapData = new ParkourMap(completeArea, spawnLocation, spectatorLocation, health, time, nightVision, checkpoints, teleporters);
			this.maps.add(mapData);
		}

		if (json.has("shuffle_map_order")) {
			this.shuffleOrder = json.getBoolean("shuffle_map_order");
		}
	}

	public String getCustomGameOverMessage() {
		return customGameOverMessage;
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