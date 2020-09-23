package com.github.ness.check.combat;

import java.time.Duration;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;

public class AutoClicker extends ListeningCheck<PlayerInteractEvent> {

	int maxCPS;

	public static final ListeningCheckInfo<PlayerInteractEvent> checkInfo = CheckInfos.forEventWithAsyncPeriodic(PlayerInteractEvent.class, Duration.ofSeconds(1));
	private int CPS; // For AutoClicker
	public AutoClicker(ListeningCheckFactory<?, PlayerInteractEvent> factory, NessPlayer player) {
		super(factory, player);
		this.maxCPS = this.ness().getNessConfig().getCheck(AutoClicker.class).getInt("maxCPS", 18);
		this.CPS = 0;
	}

	@Override
	protected void checkEvent(PlayerInteractEvent e) {
		check(e);
	}

	private void check(PlayerInteractEvent e) {
		Action action = e.getAction();
		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			NessPlayer player = this.player();
			CPS++;
			if (CPS > maxCPS && !e.getPlayer().getTargetBlock(null, 5).getType().name().contains("grass")) {
				if(player().setViolation(new Violation("AutoClicker", "CPS: " + CPS))) e.setCancelled(true);
			}
		}
	}

	@Override
	protected void checkAsyncPeriodic() {
		CPS = 0;
	}
}
