package com.github.ness.check.combat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.github.ness.CheckManager;
import com.github.ness.api.Violation;
import com.github.ness.check.AbstractCheck;
import com.github.ness.check.CheckInfo;
import com.github.ness.utility.Utility;

public class AntiKb extends AbstractCheck<EntityDamageByEntityEvent> {

	public AntiKb(CheckManager manager) {
		super(manager, CheckInfo.eventOnly(EntityDamageByEntityEvent.class));
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void checkEvent(EntityDamageByEntityEvent e) {
		Check(e);
	}

	/**
	 * Check if player move a bit
	 * 
	 * @param event
	 */
	public void Check(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			final Location from = p.getLocation();
			if(Utility.isClimbableBlock(p.getLocation().getBlock()) || Utility.specificBlockNear(from.clone(), "web") || Utility.hasKbBypass(p)) {
				return;
			}
			Bukkit.getScheduler().runTaskLater(manager.getNess(), () -> {
				Location to = p.getLocation();
				if (Math.abs(to.distanceSquared(from)) < 0.1 && !Utility.specificBlockNear(to, "water")) {
					manager.getPlayer(p).setViolation(new Violation("AntiKb", ""), null);
				}
			}, 10L);
		}
	}
}