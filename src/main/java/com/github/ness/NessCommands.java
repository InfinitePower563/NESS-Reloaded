package com.github.ness;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.ness.gui.ViolationGUI;
import com.github.ness.utility.IconMenu;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NessCommands implements CommandExecutor {

	private final NESSAnticheat ness;

	private String getPermission(String arg) {
		if (arg == null) {
			return "ness.command.usage";
		}
		switch (arg) {
		case "reload":
			return "ness.command.reload";
		case "version":
			return "ness.command.usage";
		case "report":
			return "ness.command.report";
		case "gui":
			return "ness.command.gui";
		default:
			return "ness.command.usage";
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String firstArg = (args.length >= 1) ? args[0] : null;
		if (sender.hasPermission(getPermission(firstArg))) {
			if (firstArg == null) {
				usage(sender);
			} else {
				switch (args[0].toLowerCase()) {
				case "reload":
					ness.getNessConfig().reloadConfiguration(ness);
					ness.getCheckManager().reloadChecks();
					sendMessage(sender, "&aReloaded NESS!");
					break;
				case "vl":
					showViolations(sender, (args.length >= 2) ? Bukkit.getPlayer(args[1]) : null);
					break;
				case "clear":
					clearViolations(sender, (args.length >= 2) ? Bukkit.getPlayer(args[1]) : null);
					break;
				case "version":
					sendVersion(sender);
					break;
				case "report":
					reportCommand(sender, args);
					break;
				case "gui":
					guiCommand(sender);
					break;
				default:
					usage(sender);
					break;
				}
			}
		} else {
			sendMessage(sender, ness.getNessConfig().getMessages().getString("commands.no-permission",
					"&cSorry, you cannot use this."));
		}
		return true;
	}

	private void guiCommand(CommandSender sender) {
		if (sender instanceof Player) {
			new ViolationGUI(sender).createGUI();
		}
	}

	private void reportCommand(CommandSender sender, String[] args) {
		ConfigurationSection reportconfig = ness.getNessConfig().getMessages().getConfigurationSection("commands")
				.getConfigurationSection("report-command");
		if (args.length > 1) {
			if (args.length > 2) {
				if (Bukkit.getPlayer(args[1]) == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							reportconfig.getString("player-notonline").replace("{cheater}", args[1])));
					return;
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reportconfig.getString("report-done")
						.replace("{cheater}", args[1]).replace("{cheat}", args[2])));
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.hasPermission("ness.report")) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								reportconfig.getString("staff-message").replace("{sender}", sender.getName())
										.replace("{cheater}", Bukkit.getPlayer(args[1]).getName())
										.replace("{cheat}", args[2])));
						// all.sendMessage(ChatColor.RED + "There has been a report for " +
						// ChatColor.GOLD + args[0]
						// + ChatColor.RED + "! They were reported for" + ChatColor.GOLD + " " + args[1]
						// + ChatColor.RED + "!");
					}
				}
			} else {
				sender.sendMessage(
						ChatColor.translateAlternateColorCodes('&', reportconfig.getString("missing-reason")));
			}
		} else {
			sender.sendMessage(
					ChatColor.translateAlternateColorCodes('&', reportconfig.getString("missing-arguments")));
		}
		/*
		 * if (args.length != 0) { if (args.length < 2) { sender.sendMessage(
		 * ChatColor.translateAlternateColorCodes('&',
		 * reportconfig.getString("missing-reason"))); } else { if
		 * (Bukkit.getPlayer(args[0]) == null) { sender.sendMessage(
		 * ChatColor.translateAlternateColorCodes('&',
		 * reportconfig.getString("player-notonline"))); return; } for (Player p :
		 * Bukkit.getOnlinePlayers()) { if (p.hasPermission("ness.report")) {
		 * p.sendMessage(ChatColor.translateAlternateColorCodes('&',
		 * reportconfig.getString("staff-message").replace("sender", sender.getName())
		 * .replace("{cheater}", args[0]).replace("{cheat}", args[1]))); //
		 * all.sendMessage(ChatColor.RED + "There has been a report for " + //
		 * ChatColor.GOLD + args[0] // + ChatColor.RED + "! They were reported for" +
		 * ChatColor.GOLD + " " + args[1] // + ChatColor.RED + "!"); } } } } else {
		 * sender.sendMessage( ChatColor.translateAlternateColorCodes('&',
		 * reportconfig.getString("missing-arguments"))); }
		 */
	}

	private void showViolations(CommandSender sender, Player target) {
		if (target == null) {
			sendMessage(sender, ness.getNessConfig().getMessages().getString("commands.not-found-player",
					"&cTarget player not specified, not found, or offline."));
			return;
		}
		String name = target.getName();
		sendMessage(sender, ness.getNessConfig().getMessages()
				.getString("commands.show-violations.starting", "7Listing violations...").replace("%TARGET%", name));
		String header = ness.getNessConfig().getMessages().getString("commands.show-violations.header",
				"&7Violations for &e%TARGET%");
		String body = ness.getNessConfig().getMessages().getString("commands.show-violations.body",
				"&7Hack: &e%HACK%&7. Count: %VL%");

		ness.getViolationManager().getCopyOfViolationMap(ness.getCheckManager().getPlayer(target))
				.thenAccept((violations) -> {
					sendMessage(sender, header.replace("%TARGET%", name));
					for (Map.Entry<String, Integer> entry : violations.entrySet()) {
						sendMessage(sender, body.replace("%HACK%", entry.getKey()).replace("%VL%",
								Integer.toString(entry.getValue())));
					}
				});
	}

	private void clearViolations(CommandSender sender, Player target) {
		if (target == null) {
			sendMessage(sender, ness.getNessConfig().getMessages().getString("commands.not-found-player",
					"&cTarget player not specified, not found, or offline."));
			return;
		}
		String name = target.getName();
		sendMessage(sender, ness.getNessConfig().getMessages()
				.getString("commands.clear-violations.starting", "7Clearing violations...").replace("%TARGET%", name));
		String completeMsg = ness.getNessConfig().getMessages().getString("", "&7Cleared violations for &e%TARGET%");

		ness.getViolationManager().clearViolations(ness.getCheckManager().getPlayer(target)).thenRun(() -> {
			sendMessage(sender, completeMsg.replace("%TAGET%", name));
		});
	}

	private void sendVersion(CommandSender sender) {
		sendMessage(sender, "&7Version " + ness.getDescription().getVersion());
	}

	private void usage(CommandSender sender) {
		sendMessage(sender, "&aNESS Anticheat");
		sendVersion(sender);
		sendMessage(sender,
				'\n' + "/ness toggle <dev|debug> - Toggle debug or dev mode." + '\n'
						+ "/ness vl <player> - View player violations." + '\n' + "/ness reload - Reload configuration."
						+ '\n' + '\n' + "&7Authors: " + StringUtils.join(ness.getDescription().getAuthors(), ", "));
	}

	private void sendMessage(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

}
