package Tux2.TuxTwoLib.attributes;

import Tux2.TuxTwoLib.attributes.Attribute;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.server.v1_9_R1.Item;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R1.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;

public class Attributes {

   public static ItemStack apply(ItemStack original, Attribute attribute, boolean replace) {
      try {
         if(original instanceof CraftItemStack) {
            net.minecraft.server.v1_9_R1.ItemStack ex = CraftItemStack.asNMSCopy(original);
            NBTTagCompound tag = ex.getTag();
            NBTTagList list;
            if(replace) {
               list = new NBTTagList();
            } else {
               list = tag.getList("AttributeModifiers", 10);
            }

            list.add(attribute.write());
            tag.set("AttributeModifiers", list);
            ex.setTag(tag);
            return original;
         } else {
            return original;
         }
      } catch (InstantiationException var6) {
         var6.printStackTrace();
         return original;
      } catch (IllegalAccessException var7) {
         var7.printStackTrace();
         return original;
      }
   }

   public static ItemStack apply(ItemStack original, Collection attributes, boolean replace) {
      if(attributes.size() == 0 && !replace) {
         return original;
      } else {
         try {
            net.minecraft.server.v1_9_R1.ItemStack ex = getMinecraftItemStack(original);
            NBTTagCompound tag = ex.getTag();
            if(tag == null) {
               tag = new NBTTagCompound();
            }

            NBTTagList list;
            if(replace) {
               list = new NBTTagList();
            } else {
               list = tag.getList("AttributeModifiers", 10);
            }

            Iterator var7 = attributes.iterator();

            while(var7.hasNext()) {
               Attribute attribute = (Attribute)var7.next();
               if(attribute != null) {
                  list.add(attribute.write());
               }
            }

            tag.set("AttributeModifiers", list);
            ex.setTag(tag);
            return CraftItemStack.asCraftMirror(ex);
         } catch (InstantiationException var8) {
            var8.printStackTrace();
            return original;
         } catch (IllegalAccessException var9) {
            var9.printStackTrace();
            return original;
         }
      }
   }

   public static ArrayList fromStack(ItemStack is) {
      try {
         net.minecraft.server.v1_9_R1.ItemStack ex = getMinecraftItemStack(is);
         if(ex == null) {
            return new ArrayList();
         } else {
            NBTTagCompound tag = ex.getTag();
            if(tag == null) {
               return new ArrayList();
            } else {
               NBTTagList attributes;
               if((attributes = tag.getList("AttributeModifiers", 10)) == null) {
                  return new ArrayList();
               } else {
                  ArrayList list = new ArrayList();

                  for(int i = 0; i < attributes.size(); ++i) {
                     NBTTagCompound attribute = attributes.get(i);
                     list.add(Attribute.fromTag(attribute));
                  }

                  return list;
               }
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         return new ArrayList();
      }
   }

   private static net.minecraft.server.v1_9_R1.ItemStack getMinecraftItemStack(ItemStack is) {
      if(!(is instanceof CraftItemStack)) {
         Item cis = CraftMagicNumbers.getItem(is.getType());
         if(cis == null) {
            return null;
         }

         net.minecraft.server.v1_9_R1.ItemStack e = new net.minecraft.server.v1_9_R1.ItemStack(cis, is.getAmount(), is.getDurability());
         CraftItemStack mis = CraftItemStack.asCraftMirror(e);

         try {
            Field e1 = CraftItemStack.class.getDeclaredField("handle");
            e1.setAccessible(true);
            net.minecraft.server.v1_9_R1.ItemStack mis1 = (net.minecraft.server.v1_9_R1.ItemStack)e1.get(mis);
            if(is.hasItemMeta()) {
               CraftItemStack.setItemMeta(mis1, is.getItemMeta());
            }

            return mis1;
         } catch (NoSuchFieldException var10) {
            var10.printStackTrace();
         } catch (SecurityException var11) {
            var11.printStackTrace();
         } catch (IllegalArgumentException var12) {
            var12.printStackTrace();
         } catch (IllegalAccessException var13) {
            var13.printStackTrace();
         }
      }

      if(is instanceof CraftItemStack) {
         CraftItemStack cis1 = (CraftItemStack)is;

         try {
            Field e2 = CraftItemStack.class.getDeclaredField("handle");
            e2.setAccessible(true);
            net.minecraft.server.v1_9_R1.ItemStack mis2 = (net.minecraft.server.v1_9_R1.ItemStack)e2.get(cis1);
            return mis2;
         } catch (NoSuchFieldException var6) {
            var6.printStackTrace();
         } catch (SecurityException var7) {
            var7.printStackTrace();
         } catch (IllegalArgumentException var8) {
            var8.printStackTrace();
         } catch (IllegalAccessException var9) {
            var9.printStackTrace();
         }
      }

      return null;
   }
}
