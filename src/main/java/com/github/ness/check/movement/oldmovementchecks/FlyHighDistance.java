package com.github.ness.check.movement.oldmovementchecks;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;
import com.github.ness.data.MovementValues;
import com.github.ness.data.PlayerAction;
import com.github.ness.utility.Utility;

public class FlyHighDistance extends ListeningCheck<PlayerMoveEvent> {

	public static final ListeningCheckInfo<PlayerMoveEvent> checkInfo = CheckInfos.forEvent(PlayerMoveEvent.class);

	int preVL;
	
	public FlyHighDistance(ListeningCheckFactory<?, PlayerMoveEvent> factory, NessPlayer player) {
		super(factory, player);
		preVL = 0;
	}

	@Override
	protected void checkEvent(PlayerMoveEvent e) {
		MovementValues values = player().getMovementValues();
		Player player = e.getPlayer();
		double dist = values.XZDiff;
		if (Utility.hasflybypass(player) || player.getAllowFlight() || Utility.hasVehicleNear(player, 4)
				|| player().isTeleported()) {
			return;
		}
		if (player().nanoTimeDifference(PlayerAction.VELOCITY) < 1600) {
			dist -= Math.abs(player().velocity.getX()) + Math.abs(player().velocity.getZ());
		}
		if (!values.isOnGround && dist > 0.35 && values.yDiff == 0.0
				&& this.player().getTimeSinceLastWasOnIce() >= 1000) {
			if(preVL++ > 1) {
				if(player().setViolation(new Violation("Fly", "HighDistance(OnMove)"))) e.setCancelled(true);
			}
		} else if(preVL > 0) {
			preVL--;
		}
	}

}
