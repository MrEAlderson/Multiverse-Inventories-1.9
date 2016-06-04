package Tux2.TuxTwoLib;

import Tux2.TuxTwoLib.TuxTwoInventoryPlayer;
import java.lang.reflect.Field;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class TuxTwoListener implements Listener {

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      Player player = event.getPlayer();
      if(player instanceof CraftHumanEntity) {
         CraftHumanEntity cplayer = (CraftHumanEntity)player;

         try {
            Field e = CraftHumanEntity.class.getDeclaredField("inventory");
            e.setAccessible(true);
            e.set(cplayer, new TuxTwoInventoryPlayer((CraftInventoryPlayer)e.get(cplayer)));
         } catch (NoSuchFieldException var5) {
            var5.printStackTrace();
         } catch (SecurityException var6) {
            var6.printStackTrace();
         } catch (IllegalArgumentException var7) {
            var7.printStackTrace();
         } catch (IllegalAccessException var8) {
            var8.printStackTrace();
         }
      }

   }
}
