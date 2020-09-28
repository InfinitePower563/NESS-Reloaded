package com.github.ness.check.movement.oldmovementchecks;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.github.ness.NessPlayer;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;
import com.github.ness.data.MovementValues;
import com.github.ness.utility.Utility;

public class Step extends ListeningCheck<PlayerMoveEvent> {

	public static final ListeningCheckInfo<PlayerMoveEvent> checkInfo = CheckInfos.forEvent(PlayerMoveEvent.class);

	public Step(ListeningCheckFactory<?, PlayerMoveEvent> factory, NessPlayer player) {
		super(factory, player);
	}

	@Override
	protected void checkEvent(PlayerMoveEvent e) {
		Location to = e.getTo();
		Location from = e.getFrom();
		Player player = e.getPlayer();
		MovementValues values = player().getMovementValues();
		if (Utility.hasflybypass(player) || player.getAllowFlight() || Utility.hasVehicleNear(player, 4)
				|| player().isTeleported()) {
			return;
		}
		if (to.getY() - from.getY() > 0.6 && values.groundAround && !values.AroundSlime) {
			boolean boatNear = false;
			for (Entity ent : player.getNearbyEntities(2, 2, 2)) {
				if (ent instanceof Boat)
					boatNear = true;
			}
			if (player.getVelocity().getY() < 0.43 && !boatNear) {
				flagEvent(e);
				//if(player().setViolation(new Violation("Step", "(OnMove)"))) e.setCancelled(true);
			}
		} else if (from.getY() - to.getY() > 1.5 && player.getFallDistance() == 0.0 && player.getVelocity().getY() < 0.43) {
			flagEvent(e);
			//if(player().setViolation(new Violation("Step", "(OnMove)"))) e.setCancelled(true);
		}
	}

}
