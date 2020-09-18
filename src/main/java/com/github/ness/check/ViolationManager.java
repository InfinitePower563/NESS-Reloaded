package com.github.ness.check;

import com.github.ness.NESSAnticheat;
import com.github.ness.NessPlayer;
import com.github.ness.api.impl.PlayerPunishEvent;
import com.github.ness.api.Violation;
import com.github.ness.api.ViolationAction;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ViolationManager {

	private final NESSAnticheat ness;

	private final Set<ViolationAction> actions = new CopyOnWriteArraySet<>();

	private String addViolationVariables(String message, Player player, Violation violation, int violationCount) {
		return ChatColor
				.translateAlternateColorCodes('&',
						message.replace("%PLAYER%", player.getName()).replace("%HACK%", violation.getCheck())
								.replace("%DETAILS%", violation.getDetails() + ", "))
				.replace("%VL%", Integer.toString(violationCount));
	}

	public void addDefaultActions() {
		ConfigurationSection section = ness.getNessConfig().getViolationHandling();
		if (section != null) {
			ConfigurationSection notifyStaff = section.getConfigurationSection("notify-staff");
			if (notifyStaff != null && notifyStaff.getBoolean("enable", false)) {
				final String notification = notifyStaff.getString("notification");
				if (notification != null) {
					addAction(new ViolationAction(false) {

						@Override
						public void actOn(Player player, Violation violation, int violationCount) {
							if (player.hasPermission("ness.bypass.*")
									|| player.hasPermission("ness.bypass." + violation.getCheck().toLowerCase())) {
								return;
							}
							if (violationCount > (notifyStaff.getInt("vl") - 1)) {
								String notif = addViolationVariables(notification, player, violation, violationCount);

								NessPlayer nessPlayer = ness.getCheckManager().getExistingPlayer(player);
								if (nessPlayer != null) {
									nessPlayer.sendWebhook(violation, violationCount);
								}
								if (notifyStaff.getBoolean("bungeecord", false)) {
									ByteArrayDataOutput out = ByteStreams.newDataOutput();
									out.writeUTF("NESS-Reloaded");
									out.writeUTF(notif);
									player.sendPluginMessage(ness, "BungeeCord", out.toByteArray());
								}
								for (Player staff : Bukkit.getOnlinePlayers()) {
									if (staff.hasPermission("ness.notify")
											|| staff.hasPermission("ness.notify.hacks")) {
										staff.sendMessage(notif);
									}
								}
							}
						}

					});
				}
			}
			ConfigurationSection execCmd = section.getConfigurationSection("execute-command");
			if (execCmd != null && execCmd.getBoolean("enable", false)) {
				final String command = execCmd.getString("command");
				if (command != null) {
					addAction(new ViolationAction(false) {

						@Override
						public void actOn(Player player, Violation violation, int violationCount) {
							if (violationCount > (execCmd.getInt("vl") - 1)) {
								String cmd = addViolationVariables(command, player, violation, violationCount);
								NessPlayer nessPlayer = ness.getCheckManager().getExistingPlayer(player);
								if (nessPlayer != null) {
									PlayerPunishEvent event = new PlayerPunishEvent(player, nessPlayer, violation,
											violationCount, command);
									Bukkit.getPluginManager().callEvent(event);
									if (!event.isCancelled()) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
									}
								}
							}
						}

					});
				}
			}
		}
	}

	public void addAction(ViolationAction action) {
		actions.add(action);
	}

	public void initiatePeriodicTask() {
		ness.getExecutor().scheduleWithFixedDelay(() -> {
			ness.getCheckManager().forEachPlayer((player) -> {

				// Atomicaly get the existing violation and set it to null
				final Violation previous = player.violation.getAndSet(null);
				if (previous != null) {

					Map<String, Integer> checkViolationCounts = player.checkViolationCounts;
					int violationCount = checkViolationCounts.get(previous.getCheck());

					// actions we have to run on the main thread
					Set<ViolationAction> syncActions = null;

					for (ViolationAction action : actions) {
						if (action.canRunAsync()) {
							action.actOn(player.getPlayer(), previous, violationCount);
						} else {
							if (syncActions == null) {
								syncActions = new HashSet<>();
							}
							syncActions.add(action);
						}
					}
					if (syncActions != null) {
						final Set<ViolationAction> syncActionsFinal = syncActions;
						Bukkit.getScheduler().runTask(ness, () -> {
							for (ViolationAction syncAction : syncActionsFinal) {
								syncAction.actOn(player.getPlayer(), previous, violationCount);
							}
						});
					}
				}
			});
		}, 1L, 1L, TimeUnit.SECONDS);
	}

}
