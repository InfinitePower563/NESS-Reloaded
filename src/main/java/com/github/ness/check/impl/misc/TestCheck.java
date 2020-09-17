package com.github.ness.check.impl.misc;

import com.github.ness.check.CheckManager;
import com.github.ness.NESSPlayer;
import com.github.ness.check.AbstractCheck;
import com.github.ness.check.CheckInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class TestCheck extends AbstractCheck<PlayerMoveEvent> {

    public TestCheck(CheckManager manager) {
        super(manager, CheckInfo.eventOnly(PlayerMoveEvent.class));
    }

    @Override
    protected void checkEvent(PlayerMoveEvent event) {
        NESSPlayer nessPlayer = this.getNessPlayer(event.getPlayer());
        double pitch = nessPlayer.getMovementValues().pitchDiff;
        double yaw = nessPlayer.getMovementValues().yawDiff;
        Player p = event.getPlayer();
    }

}