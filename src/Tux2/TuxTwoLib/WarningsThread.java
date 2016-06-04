package Tux2.TuxTwoLib;

import Tux2.TuxTwoLib.TuxTwoLib;
import java.util.Iterator;
import java.util.LinkedList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WarningsThread implements Runnable {

   LinkedList players = new LinkedList();
   TuxTwoLib plugin;


   public WarningsThread(TuxTwoLib plugin) {
      this.plugin = plugin;
   }

   public void addPlayer(Player player) {
      this.players.add(player);
   }

   public void run() {
      Iterator var2 = this.players.iterator();

      while(var2.hasNext()) {
         Player player = (Player)var2.next();
         if(player.isOnline()) {
            if(this.plugin.incompatiblemcversion && this.plugin.hasupdate) {
               player.sendMessage(ChatColor.RED + "WARNING! A Minecraft version update has forced an update of the TuxTwoLib to version " + this.plugin.newversion + ". Please restart your server.");
            } else if(this.plugin.hasupdate) {
               player.sendMessage(ChatColor.RED + "Your version of TuxTwoLib has been updated to version " + this.plugin.newversion + "! Please restart the server.");
            } else if(this.plugin.incompatiblemcversion && !this.plugin.newversion.equals("")) {
               player.sendMessage(ChatColor.DARK_RED + "Your version of TuxTwoLib is incompatible with this version of Craftbukkit! Update to version " + this.plugin.newversion + " immediately!");
            } else if(this.plugin.incompatiblemcversion) {
               player.sendMessage(ChatColor.DARK_RED + "Your version of TuxTwoLib is incompatible with this version of Craftbukkit! Update immediately!");
            } else if(this.plugin.updatefailed) {
               player.sendMessage(ChatColor.RED + "TuxTwoLib failed to update automatically! Please update to version " + this.plugin.newversion + " manually.");
            } else if(!this.plugin.newversion.equals("")) {
               player.sendMessage(ChatColor.RED + "Your version of TuxTwoLib is out of date! Please update to version " + this.plugin.newversion);
            }
         }
      }

      this.players.clear();
   }
}
