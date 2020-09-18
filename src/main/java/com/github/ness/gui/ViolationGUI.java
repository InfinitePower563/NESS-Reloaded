package com.github.ness.gui;

import com.github.ness.NESSAnticheat;
import com.github.ness.NessPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ViolationGUI {

	private final CommandSender sender;

	public ViolationGUI(CommandSender send) {
		this.sender = send;
	}

	public void createGUI() {
		Player p = (Player) sender;
		IconMenu menu = new IconMenu("§c§lNESS Violation Manager", 45, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				/*
				 * if (Bukkit.getPlayer(event.getName()) == null) {
				 * p.sendMessage("This player isn't online!"); return; }
				 */
			}
		}, NESSAnticheat.getInstance());
		int i = 0;
		for (Player cheater : Bukkit.getOnlinePlayers()) {
			NessPlayer np = NESSAnticheat.getInstance().getCheckManager().getExistingPlayer(cheater);
			if (np == null) {
				continue;
			}
			i++;
			List<String> lore = new ArrayList<String>();
			lore.add("Cheats:");

			for (String s : np.checkViolationCounts.keySet()) {
				lore.add(s + " VL: " + np.checkViolationCounts.getOrDefault(s, 0));
			}
			menu.setOption(i - 1, getPlayerHead(cheater), cheater.getName(), lore);
		}
		menu.open(p);
	}

	@SuppressWarnings("deprecation")
	public ItemStack getPlayerHead(Player player) {
		ItemStack playerhead;
		if (NESSAnticheat.getInstance().getVersion() > 1122) {
			playerhead = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1, (short) 3);
		} else {
			playerhead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		}
		SkullMeta playerheadmeta = (SkullMeta) playerhead.getItemMeta();
		playerheadmeta.setOwner(player.getName());
		playerheadmeta.setDisplayName(player.getName());
		playerheadmeta.setLocalizedName(player.getName());
		playerhead.setItemMeta(playerheadmeta);
		return playerhead;
	}
}
