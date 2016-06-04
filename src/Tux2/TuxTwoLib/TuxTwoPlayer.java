package Tux2.TuxTwoLib;

import com.mojang.authlib.GameProfile;
import java.io.File;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.MinecraftServer;
import net.minecraft.server.v1_9_R1.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TuxTwoPlayer {

   public static Player getOfflinePlayer(OfflinePlayer player) {
      Object pplayer = null;

      try {
         File e = new File(((World)Bukkit.getWorlds().get(0)).getWorldFolder(), "playerdata");
         File[] var6;
         int var5 = (var6 = e.listFiles()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            File playerfile = var6[var4];
            String filename = playerfile.getName();
            String playername = filename.substring(0, filename.length() - 4);
            if(playername.trim().equalsIgnoreCase(player.getUniqueId().toString())) {
               MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
               EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), new GameProfile(player.getUniqueId(), player.getName()), new PlayerInteractManager(server.getWorldServer(0)));
               CraftPlayer target = entity == null?null:entity.getBukkitEntity();
               if(target != null) {
                  target.loadData();
                  return target;
               }
            }
         }

         return (Player)pplayer;
      } catch (Exception var12) {
         return null;
      }
   }

   @Deprecated
   public static Player getOfflinePlayer(String player) {
      OfflinePlayer oplayer = Bukkit.getOfflinePlayer(player);
      return getOfflinePlayer(oplayer);
   }
}
