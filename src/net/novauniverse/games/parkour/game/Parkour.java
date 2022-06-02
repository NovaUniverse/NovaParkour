package net.novauniverse.games.parkour.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import net.novauniverse.games.parkour.NovaParkour;
import net.novauniverse.games.parkour.game.config.ParkourCheckpoint;
import net.novauniverse.games.parkour.game.config.ParkourConfig;
import net.novauniverse.games.parkour.game.config.ParkourMap;
import net.novauniverse.games.parkour.game.config.TeleporterConfig;
import net.novauniverse.games.parkour.game.event.ParkourEvent;
import net.novauniverse.games.parkour.game.event.ParkourPlacementEvent;
import net.novauniverse.games.parkour.game.event.ParkourPlayerCompleteRoundEvent;
import net.novauniverse.games.parkour.game.event.ParkourPlayerFailRoundEvent;
import net.novauniverse.games.parkour.game.event.ParkourPlayerReachTeleporterEvent;
import net.novauniverse.games.parkour.game.event.ParkourRoundEndEvent;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.commons.utils.TextUtils;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependantUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.NovaCoreGameVersion;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependantSound;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.MapGame;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerEliminationReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;
import net.zeeraa.novacore.spigot.utils.XYLocation;

public class Parkour extends MapGame implements Listener {
	private boolean started;
	private boolean ended;

	private ParkourConfig config;

	private List<ParkourMap> maps;
	private List<UUID> remainingPlayers;

	private HashMap<UUID, ParkourCheckpoint> playerActiveCheckpoint;

	private int timeLeft;

	private Task countdownTask;
	private Task checkTask;

	private XYLocation activeChunkLocation;

	private Map<UUID, Integer> dropperScore;
	private Map<UUID, Integer> deaths;

	public Parkour(Plugin plugin) {
		super(plugin);

		this.started = false;
		this.ended = false;

		this.remainingPlayers = new ArrayList<>();

		this.maps = new ArrayList<ParkourMap>();

		this.timeLeft = -1;

		this.activeChunkLocation = null;

		this.dropperScore = new HashMap<>();
		this.deaths = new HashMap<>();

		this.playerActiveCheckpoint = new HashMap<UUID, ParkourCheckpoint>();

		this.countdownTask = new SimpleTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (timeLeft >= 0) {
					timeLeft--;
					if (timeLeft == 0) {
						remainingPlayers.forEach(uuid -> {
							ParkourEvent event = new ParkourPlayerFailRoundEvent(uuid);
							Bukkit.getServer().getPluginManager().callEvent(event);

							Player player = Bukkit.getServer().getPlayer(uuid);
							if (player != null) {
								VersionIndependantSound.WITHER_HURT.play(player);
								VersionIndependantUtils.get().sendTitle(player, ChatColor.RED + "Failed", ChatColor.RED + "time's up", 10, 60, 10);
							}
						});

						Bukkit.getServer().getOnlinePlayers().forEach(player -> {
							player.setGameMode(GameMode.SPECTATOR);
							Bukkit.getServer().getOnlinePlayers().forEach(player2 -> {
								if (player2 != player) {
									player.showPlayer(player2);
								}
							});
						});

						Bukkit.getServer().broadcastMessage(ChatColor.RED + "Some players did not complete this round in time");

						beginNextRoundWait();
					}
				}
			}
		}, 20L);

		checkTask = new SimpleTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (players.size() == 0) {
					endGame(GameEndReason.NO_PLAYERS_LEFT);
					return;
				}

				if (maps.size() > 0) {
					ParkourMap map = maps.get(0);

					/*
					 * Bukkit.getServer().getOnlinePlayers().forEach(player -> { if
					 * (player.getGameMode() == GameMode.SPECTATOR) { Location location =
					 * player.getLocation();
					 * 
					 * boolean shouldTp = false; double x = location.getX(); double z =
					 * location.getZ();
					 * 
					 * if (x < map.getArea().getPosition1().getX()) { x =
					 * map.getArea().getPosition1().getX(); shouldTp = true; }
					 * 
					 * if (x > map.getArea().getPosition2().getX()) { x =
					 * map.getArea().getPosition2().getX(); shouldTp = true; }
					 * 
					 * if (z < map.getArea().getPosition1().getZ()) { z =
					 * map.getArea().getPosition1().getZ(); shouldTp = true; }
					 * 
					 * if (z > map.getArea().getPosition2().getZ()) { z =
					 * map.getArea().getPosition2().getZ(); shouldTp = true; }
					 * 
					 * if (shouldTp) { Location target = new Location(location.getWorld(), x,
					 * location.getY(), z, location.getYaw(), location.getPitch());
					 * player.teleport(target); } } });
					 */

					if (timeLeft > 0) {
						// Checkpoints and teleporters
						if (hasActiveMap()) {
							Bukkit.getServer().getOnlinePlayers().forEach(player -> {
								if (isPlayerInGame(player)) {
									if (player.getGameMode() != GameMode.SPECTATOR) {
										// Teleporters
										TeleporterConfig teleporter = map.getTeleporters().stream().filter(t -> t.getActivationArea().isInsideBlock(player.getLocation().toVector())).findFirst().orElse(null);
										if (teleporter != null) {
											Event event = new ParkourPlayerReachTeleporterEvent(teleporter, player);
											Bukkit.getServer().getPluginManager().callEvent(event);
											player.teleport(teleporter.getTargetLocation(), TeleportCause.PLUGIN);
										}

										// Checkpoint
										ParkourCheckpoint checkpoint = map.getCheckpoints().stream().filter(c -> c.getUnlockArea().isInsideBlock(player.getLocation().toVector())).findFirst().orElse(null);
										if (checkpoint != null) {
											boolean isNew = false;
											if (playerActiveCheckpoint.containsKey(player.getUniqueId())) {
												if (playerActiveCheckpoint.get(player.getUniqueId()).getValue() < checkpoint.getValue()) {
													isNew = true;
												}
											} else {
												isNew = true;
											}

											if (isNew) {
												if (!checkpoint.isNoMessage()) {
													String message = ChatColor.GREEN + "" + ChatColor.BOLD + "Reached checkpoint";
													String title = ChatColor.GREEN + "Checkpoint";

													if (checkpoint.getMessage() != null) {
														message = checkpoint.getMessage();
													}

													if (checkpoint.getTitle() != null) {
														title = checkpoint.getTitle();
													}

													if (message.length() > 0) {
														player.sendMessage(message);
													}

													if (title.length() > 0) {
														VersionIndependantUtils.get().sendTitle(player, title, "", 10, 20, 10);
													}
												}
												playerActiveCheckpoint.put(player.getUniqueId(), checkpoint);
											}
										}
									}
								}
							});
						}

						// Check completed
						List<UUID> toRemove = new ArrayList<>();
						remainingPlayers.forEach(uuid -> {
							Player player = Bukkit.getServer().getPlayer(uuid);
							if (player != null) {
								if (player.getGameMode() != GameMode.SPECTATOR) {
									if (map.getCompleteArea().isInsideBlock(player.getLocation().toVector())) {
										toRemove.add(uuid);
									}
								}
							}
						});

						// Remove completed players
						toRemove.forEach(uuid -> {
							Player player = Bukkit.getServer().getPlayer(uuid);
							remainingPlayers.remove(uuid);
							ChatColor color = ChatColor.AQUA;
							if (NovaCore.getInstance().hasTeamManager()) {
								Team team = TeamManager.getTeamManager().getPlayerTeam(uuid);
								color = team.getTeamColor();
							}
							Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Completed> " + color + ChatColor.BOLD + player.getName() + ChatColor.GREEN + ChatColor.BOLD + " completed the level");

							int placement = players.size() - remainingPlayers.size();

							ParkourEvent event = new ParkourPlayerCompleteRoundEvent(player, remainingPlayers.size(), players.size(), placement);
							Bukkit.getServer().getPluginManager().callEvent(event);

							UUID scoreTargetUUID = uuid;
							if (NovaCore.getInstance().hasTeamManager()) {
								Team team = TeamManager.getTeamManager().getPlayerTeam(uuid);
								if (team == null) {
									scoreTargetUUID = null;
								} else {
									scoreTargetUUID = team.getTeamUuid();
								}
							}

							if (scoreTargetUUID != null) {
								Integer score = remainingPlayers.size() + 1;
								if (dropperScore.containsKey(scoreTargetUUID)) {
									score += dropperScore.get(scoreTargetUUID);
								}
								dropperScore.put(scoreTargetUUID, score);
							}

							teleportPlayer(player);
							VersionIndependantSound.ORB_PICKUP.play(player);
							VersionIndependantUtils.get().sendTitle(player, ChatColor.GREEN + "Completed", ChatColor.GREEN + TextUtils.ordinal(placement) + " place", 10, 60, 10);
						});

						// End the game
						if (remainingPlayers.size() == 0) {
							Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "All players completed the level");
							timeLeft = -1;
							beginNextRoundWait();
						}
					}
				}
			}
		}, 4L);
	}

	public List<ParkourMap> getMaps() {
		return maps;
	}

	public Map<UUID, Integer> getDropperScore() {
		return dropperScore;
	}

	@Override
	public String getName() {
		return "dropper";
	}

	@Override
	public String getDisplayName() {
		return "Dropper";
	}

	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return PlayerQuitEliminationAction.DELAYED;
	}

	@Override
	public boolean eliminatePlayerOnDeath(Player player) {
		return false;
	}

	@Override
	public boolean isPVPEnabled() {
		return false;
	}

	@Override
	public boolean autoEndGame() {
		return false;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		return false;
	}

	public List<UUID> getRemainingPlayers() {
		return remainingPlayers;
	}

	@Override
	public void onPlayerEliminated(OfflinePlayer player, Entity killer, PlayerEliminationReason reason, int placement) {
		if (remainingPlayers.contains(player.getUniqueId())) {
			remainingPlayers.remove(player.getUniqueId());
		}
	}

	@Override
	public void onStart() {
		if (started) {
			return;
		}

		config = (ParkourConfig) this.getActiveMap().getMapData().getMapModule(ParkourConfig.class);
		if (config == null) {
			Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Failed to start: Configuration error");
			Log.fatal("Parkour", "Failed to start. Missing map module parkour.config");
			return;
		}

		if (config.getMaps().size() == 0) {
			Log.error("Parkour", "Failed to start: 0 maps configured");
			return;
		}

		started = true;

		world.setDifficulty(Difficulty.PEACEFUL);

		config.getMaps().forEach(map -> maps.add(map));

		ParkourMap map = maps.get(0);

		timeLeft = map.getTime();
		activeChunkLocation = new XYLocation(map.getSpawnLocation().getChunk().getX(), map.getSpawnLocation().getChunk().getZ());
		map.getSpawnLocation().getChunk().load();

		players.forEach(uuid -> {
			remainingPlayers.add(uuid);

			if (!NovaCore.getInstance().hasTeamManager()) {
				dropperScore.put(uuid, 0);
			}
		});

		if (NovaCore.getInstance().hasTeamManager()) {
			TeamManager.getTeamManager().getTeams().forEach(team -> dropperScore.put(team.getTeamUuid(), 0));
		}

		if (config.isShuffleOrder()) {
			Collections.shuffle(maps, random);
		}

		world.setSpawnLocation(map.getSpawnLocation().getBlockX(), map.getSpawnLocation().getBlockY(), map.getSpawnLocation().getBlockZ());

		Bukkit.getServer().getOnlinePlayers().forEach(player -> teleportPlayer(player));

		Task.tryStartTask(checkTask);
		Task.tryStartTask(countdownTask);

		this.sendBeginEvent();
	}

	@Override
	public void onEnd(GameEndReason reason) {
		if (ended) {
			return;
		}

		ended = true;

		Bukkit.getServer().getOnlinePlayers().forEach(player -> player.setGameMode(GameMode.SPECTATOR));

		if (reason != GameEndReason.OPERATOR_ENDED_GAME) {
			List<Entry<UUID, Integer>> list = new ArrayList<>(dropperScore.entrySet());
			list.sort(Entry.comparingByValue());
			Collections.reverse(list);

			int maxEntries = 5;
			int max = (list.size() > maxEntries) ? maxEntries : list.size();
			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "-- Top " + max + (TeamManager.hasTeamManager() ? " teams" : " players") + " --");

			for (int i = 0; i < list.size(); i++) {
				Entry<UUID, Integer> entry = list.get(i);
				String name = "your mom";
				PlacementType type = PlacementType.PLAYER;

				if (TeamManager.hasTeamManager()) {
					Team team = TeamManager.getTeamManager().getTeamByTeamUUID(entry.getKey());
					name = team.getDisplayName();
					type = PlacementType.TEAM;
				} else {
					OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(entry.getKey());
					name = player.getName();
				}
				Log.debug("Placement", (i + 1) + ": " + name + " Score: " + entry.getValue());

				ParkourEvent event = new ParkourPlacementEvent(entry.getKey(), type, i + 1, entry.getValue());
				Bukkit.getServer().getPluginManager().callEvent(event);
			}

			for (int i = 0; i < max; i++) {
				Entry<UUID, Integer> entry = list.get(i);
				String name = "your mom";
				ChatColor color = ChatColor.AQUA;
				if (TeamManager.hasTeamManager()) {
					Team team = TeamManager.getTeamManager().getTeamByTeamUUID(entry.getKey());
					name = team.getDisplayName();
					color = team.getTeamColor();
				} else {
					OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(entry.getKey());
					name = player.getName();
				}
				Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + TextUtils.ordinal(i + 1) + " place: " + color + ChatColor.BOLD + name + ChatColor.AQUA + ChatColor.BOLD + " with " + entry.getValue() + " points");
			}
		}

		Task.tryStopTask(checkTask);
		Task.tryStopTask(countdownTask);
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public int getDeaths(UUID uuid) {
		if (deaths.containsKey(uuid)) {
			return deaths.get(uuid);
		}
		return 0;
	}

	public void beginNextRoundWait() {
		ParkourEvent event = new ParkourRoundEndEvent();
		Bukkit.getServer().getPluginManager().callEvent(event);

		Log.trace("Parkour#beginNextRoundWait()", "remainingPlayers.size(): " + remainingPlayers.size());

		playerActiveCheckpoint.clear();

		remainingPlayers.forEach(uuid -> {
			Player player = Bukkit.getServer().getPlayer(uuid);
			if (player != null) {
				ChatColor color = ChatColor.AQUA;
				if (NovaCore.getInstance().hasTeamManager()) {
					Team team = TeamManager.getTeamManager().getPlayerTeam(uuid);
					color = team.getTeamColor();
				}
				Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Failed> " + color + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " did not complete the level in time");
			}
		});

		remainingPlayers.clear();

		if (maps.size() <= 1) {
			if(config.getCustomGameOverMessage() != null) {
				if(config.getCustomGameOverMessage().length() > 0) {
					Bukkit.getServer().broadcastMessage(config.getCustomGameOverMessage());
				}
			} else {
				Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Game Over. No more rounds remaining");	
			}
			this.endGame(GameEndReason.ALL_FINISHED);
			return;
		}

		Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Next round starts in 5 seconds");
		new BukkitRunnable() {
			@Override
			public void run() {
				maps.remove(0);
				ParkourMap map = maps.get(0);
				activeChunkLocation = new XYLocation(map.getSpawnLocation().getChunk().getX(), map.getSpawnLocation().getChunk().getZ());
				map.getSpawnLocation().getChunk().load();
				world.setSpawnLocation(map.getSpawnLocation().getBlockX(), map.getSpawnLocation().getBlockY(), map.getSpawnLocation().getBlockZ());
				players.forEach(uuid -> remainingPlayers.add(uuid));
				timeLeft = map.getTime();
				Bukkit.getServer().getOnlinePlayers().forEach(player -> teleportPlayer(player));
			}
		}.runTaskLater(getPlugin(), 100L);
	}

	public void teleportPlayer(Player player) {
		if (maps.size() == 0) {
			return;
		}

		ParkourMap map = maps.get(0);

		PlayerUtils.clearPotionEffects(player);
		PlayerUtils.clearPlayerInventory(player);
		PlayerUtils.resetPlayerXP(player);

		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20);

		if (players.contains(player.getUniqueId()) && remainingPlayers.contains(player.getUniqueId())) {
			PlayerUtils.setMaxHealth(player, map.getHealth());
			player.setHealth(map.getHealth());
			player.setGameMode(GameMode.ADVENTURE);

			Bukkit.getServer().getOnlinePlayers().forEach(player2 -> {
				if (player2 != player) {
					player.hidePlayer(player2);
				}
			});

			Location location = map.getSpawnLocation();
			if (playerActiveCheckpoint.containsKey(player.getUniqueId())) {
				location = playerActiveCheckpoint.get(player.getUniqueId()).getSpawnLocation();
			}
			player.teleport(location, TeleportCause.PLUGIN);
		} else {
			PlayerUtils.resetMaxHealth(player);

			Bukkit.getServer().getOnlinePlayers().forEach(player2 -> {
				if (player2 != player) {
					player.showPlayer(player2);
				}
			});

			player.setGameMode(GameMode.SPECTATOR);
			player.teleport(map.getSpectatorLocation(), TeleportCause.PLUGIN);
		}

		if (map.isNightVision()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 0, false, false));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnload(ChunkUnloadEvent e) {
		if (!ended) {
			if (activeChunkLocation != null) {
				if (e.getChunk().getX() == activeChunkLocation.getX() && e.getChunk().getZ() == activeChunkLocation.getY()) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();
		Integer deaths = 1;
		if (this.deaths.containsKey(player.getUniqueId())) {
			deaths += this.deaths.get(player.getUniqueId());
		}
		this.deaths.put(player.getUniqueId(), deaths);

		if (NovaCore.getInstance().getVersionIndependentUtils().getNovaCoreGameVersion().isAfterOrEqual(NovaCoreGameVersion.V_1_16)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					player.spigot().respawn();
				}
			}.runTaskLater(getPlugin(), 5L);
		} else {
			player.spigot().respawn();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (started) {
			Player player = e.getPlayer();
			this.teleportPlayer(player);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (started) {
			Location location = getWorld().getSpawnLocation();

			Player player = e.getPlayer();
			if (playerActiveCheckpoint.containsKey(player.getUniqueId())) {
				location = playerActiveCheckpoint.get(player.getUniqueId()).getSpawnLocation();
			}

			if (maps.size() > 0) {
				ParkourMap map = maps.get(0);
				location = map.getSpawnLocation();
			}

			e.setRespawnLocation(location);
		}
	}

	@Override
	public void onPlayerRespawn(Player player) {
		if (started) {
			new BukkitRunnable() {
				@Override
				public void run() {
					teleportPlayer(player);
				}
			}.runTaskLater(NovaParkour.getInstance(), 1L);
		}
	}
}