package com.github.ness.check.movement.fly;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.github.ness.NESSAnticheat;
import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.AbstractCheck;
import com.github.ness.check.CheckFactory;
import com.github.ness.check.CheckInfo;
import com.github.ness.data.ImmutableLoc;
import com.github.ness.data.MovementValues;
import com.github.ness.data.PlayerAction;
import com.github.ness.utility.ReflectionUtility;
import com.github.ness.utility.Utility;

public class FlyFalseGround extends AbstractCheck<PlayerMoveEvent> {

	public static final CheckInfo<PlayerMoveEvent> checkInfo = CheckInfo.eventOnly(PlayerMoveEvent.class);

	public FlyFalseGround(CheckFactory<?> factory, NessPlayer player) {
		super(factory, player);
	}

	@Override
	protected void checkEvent(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		NessPlayer nessPlayer = this.player();
		MovementValues movementValues = nessPlayer.getMovementValues();
		if (Bukkit.getVersion().contains("1.8") || movementValues.AroundLily || movementValues.AroundCarpet
				|| movementValues.AroundSnow || NESSAnticheat.getInstance().getVersion() > 1152 || ReflectionUtility.getBlockName(player, ImmutableLoc.of(player.getLocation().clone().add(0, -0.5, 0)))
				.contains("scaffolding")) {
			return;
		}
		if (nessPlayer.nanoTimeDifference(PlayerAction.VELOCITY) < 1500 && nessPlayer.velocity.getY() > 0.35) {
			return;
		}
		if (!nessPlayer.isTeleported() && player.getNearbyEntities(2, 2, 2).isEmpty() && !Utility.hasflybypass(player)
				&& player.isOnline() && !movementValues.AroundSlime && !player.isInsideVehicle()
				&& !Utility.specificBlockNear(e.getTo().clone(), "web")) {
			if (player.isOnGround() && !Utility.groundAround(e.getTo())) {
				nessPlayer.setViolation(new Violation("Fly", "FalseGround"), e);
			} else if (player.isOnGround() && !Utility.isMathematicallyOnGround(e.getTo().getY())) {
				nessPlayer.setViolation(new Violation("Fly", "FalseGround1"), e);
			}
		}
	}
}
