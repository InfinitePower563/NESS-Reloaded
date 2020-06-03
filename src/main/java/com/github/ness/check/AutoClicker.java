package com.github.ness.check;

import java.util.concurrent.TimeUnit;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.ness.CheckManager;
import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;

public class AutoClicker extends AbstractCheck<PlayerInteractEvent> {

	public AutoClicker(CheckManager manager) {
		super(manager, CheckInfo.eventWithAsyncPeriodic(PlayerInteractEvent.class, 1, TimeUnit.SECONDS));
		// TODO Auto-generated constructor stub
	}

	@Override
	void checkEvent(PlayerInteractEvent e) {
		Check(e);
		Check1(e);
	}
	
	
	void Check(PlayerInteractEvent e) {
		Action action = e.getAction();
		if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			NessPlayer player = manager.getPlayer(e.getPlayer());
			player.setCPS(player.getCPS()+1);
			if(player.getCPS()>14) {
				player.setViolation(new Violation("AutoClicker","MaxCPS: " + player.getCPS()));
			}
		}
	}
	
	void Check1(PlayerInteractEvent e) {
		
	}
	
	@Override
	void checkAsyncPeriodic(NessPlayer player) {
		player.setCPS(0);
	}
}