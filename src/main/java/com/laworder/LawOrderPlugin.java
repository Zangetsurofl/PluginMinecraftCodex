package com.laworder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class LawOrderPlugin extends JavaPlugin {
    private final Map<UUID,Integer> wanted = new HashMap<>();
    private final Map<UUID,Long> jailedUntil = new HashMap<>();
    private final Map<UUID,Boolean> duty = new HashMap<>();
    private final Map<String,Set<UUID>> gangs = new HashMap<>();

    @Override public void onEnable() {
        saveDefaultConfig();
        getCommand("duty").setExecutor(this::command);
        getCommand("wanted").setExecutor(this::command);
        getCommand("arrest").setExecutor(this::command);
        getCommand("fine").setExecutor(this::command);
        getCommand("crime").setExecutor(this::command);
        getCommand("hide").setExecutor(this::command);
        getCommand("gang").setExecutor(this::command);
        getServer().getPluginManager().registerEvents(new LawListener(this), this);
    }
    public int wanted(Player p){ return wanted.getOrDefault(p.getUniqueId(),0); }
    public boolean police(CommandSender s){ return s.hasPermission("laworder.police") || s.hasPermission("laworder.admin"); }
    public boolean criminal(CommandSender s){ return s.hasPermission("laworder.criminal") || s.hasPermission("laworder.admin"); }
    private boolean player(CommandSender s){ if(!(s instanceof Player)){s.sendMessage("Players only.");return false;} return true; }
    private boolean target(CommandSender s,String[] a){ if(a.length<2){s.sendMessage("Usage: /"+a[0]+" <player>");return false;} return true; }
    private boolean command(CommandSender s, Command c, String label, String[] a){
        if(label.equalsIgnoreCase("duty")){ if(!police(s)||!player(s))return true; boolean on=a.length>0&&a[0].equalsIgnoreCase("on"); duty.put(((Player)s).getUniqueId(),on); s.sendMessage("Duty: "+(on?"ON":"OFF")); return true; }
        if(label.equalsIgnoreCase("crime")){ if(!criminal(s)||!player(s))return true; Player p=(Player)s; int n=Math.min(getConfig().getInt("wanted.max-level",5),wanted(p)+1); wanted.put(p.getUniqueId(),n); p.sendMessage("Crime committed. Wanted level: "+n); return true; }
        if(label.equalsIgnoreCase("hide")){ if(!criminal(s)||!player(s))return true; Player p=(Player)s; double chance=getConfig().getDouble("hide.success-chance",0.35); if(Math.random()<chance){wanted.put(p.getUniqueId(),0);p.sendMessage("You successfully hid your identity.");}else p.sendMessage("You failed to hide."); return true; }
        if(label.equalsIgnoreCase("wanted")){ if(!police(s)||!target(s,a))return true; Player p=Bukkit.getPlayerExact(a[1]); if(p==null){s.sendMessage("Player not found.");return true;} int level=a.length>2?Integer.parseInt(a[2]):1; wanted.put(p.getUniqueId(),Math.max(0,Math.min(5,level))); s.sendMessage("Wanted level set."); return true; }
        if(label.equalsIgnoreCase("arrest")){ if(!police(s)||!target(s,a))return true; Player p=Bukkit.getPlayerExact(a[1]); if(p==null){s.sendMessage("Player not found.");return true;} if(wanted(p)<1){s.sendMessage("Player is not wanted.");return true;} jailedUntil.put(p.getUniqueId(),System.currentTimeMillis()+getConfig().getLong("jail.duration-seconds",900)*1000); p.teleport(jail()); p.sendMessage("You have been arrested."); return true; }
        if(label.equalsIgnoreCase("fine")){ if(!police(s)||!target(s,a))return true; s.sendMessage("Fine recorded: "+a[1]+" "+(a.length>2?a[2]:"0")); return true; }
        if(label.equalsIgnoreCase("gang")){ if(!criminal(s)||!player(s))return true; if(a.length>=2&&a[0].equalsIgnoreCase("create")){gangs.putIfAbsent(a[1],new HashSet<>());gangs.get(a[1]).add(((Player)s).getUniqueId());s.sendMessage("Gang created/joined: "+a[1]);} else if(a.length>=1&&a[0].equalsIgnoreCase("leave")){gangs.values().forEach(x->x.remove(((Player)s).getUniqueId()));s.sendMessage("You left your gang.");} else s.sendMessage("/gang create <name> | /gang leave"); return true; }
        return false;
    }
    private Location jail(){ return new Location(Bukkit.getWorld(getConfig().getString("jail.world","world")),getConfig().getDouble("jail.x"),getConfig().getDouble("jail.y"),getConfig().getDouble("jail.z")); }
    public boolean isJailed(UUID id){return jailedUntil.getOrDefault(id,0L)>System.currentTimeMillis();}
    public Location jailLocation(){return jail();}
}
