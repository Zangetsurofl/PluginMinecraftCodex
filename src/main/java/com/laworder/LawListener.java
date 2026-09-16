package com.laworder;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public final class LawListener implements Listener {
  private final LawOrderPlugin plugin;
  public LawListener(LawOrderPlugin plugin){this.plugin=plugin;}
  @EventHandler public void onMove(PlayerMoveEvent e){
    Player p=e.getPlayer();
    if(plugin.isJailed(p.getUniqueId()) && e.getTo()!=null && e.getTo().distanceSquared(plugin.jailLocation())>100){e.setTo(plugin.jailLocation());}
  }
  @EventHandler public void onJoin(PlayerJoinEvent e){if(plugin.isJailed(e.getPlayer().getUniqueId()))e.getPlayer().teleport(plugin.jailLocation());}
}
