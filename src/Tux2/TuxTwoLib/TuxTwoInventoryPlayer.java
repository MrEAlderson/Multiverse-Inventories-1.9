package Tux2.TuxTwoLib;

import Tux2.TuxTwoLib.InventoryChangeEvent;
import java.util.HashMap;
import net.minecraft.server.v1_9_R1.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TuxTwoInventoryPlayer extends CraftInventoryPlayer {

   public TuxTwoInventoryPlayer(PlayerInventory inventory) {
      super(inventory);
   }

   public TuxTwoInventoryPlayer(CraftInventoryPlayer inventory) {
      super(inventory.getInventory());
   }

   public void setArmorContents(ItemStack[] items) {
      super.setArmorContents(items);
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), 0, true, items);
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
   }

   public void setBoots(ItemStack boots) {
      super.setBoots(boots);
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), 0, true, new ItemStack[]{boots});
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
   }

   public void setChestplate(ItemStack chestplate) {
      super.setChestplate(chestplate);
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), 2, true, new ItemStack[]{chestplate});
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
   }

   public void setHelmet(ItemStack helmet) {
      super.setHelmet(helmet);
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), 3, true, new ItemStack[]{helmet});
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
   }

   public void setLeggings(ItemStack leggings) {
      super.setLeggings(leggings);
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), 1, true, new ItemStack[]{leggings});
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
   }

   public HashMap addItem(ItemStack ... items) {
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), items);
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
      return super.addItem(items);
   }

   public void setContents(ItemStack[] items) {
      super.setContents(items);
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), 0, items);
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
   }

   public void setItem(int index, ItemStack item) {
      super.setItem(index, item);
      InventoryChangeEvent eventcall = new InventoryChangeEvent((Player)this.getHolder(), index, new ItemStack[]{item});
      Bukkit.getServer().getPluginManager().callEvent(eventcall);
   }
}
