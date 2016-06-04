package Tux2.TuxTwoLib;

import java.lang.reflect.Field;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class TuxTwoPlayerHead {
	public static ItemStack getHead(ItemStack is, NMSHeadData data){
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static NMSHeadData getHeadData(ItemStack is){
		SkullMeta meta = (SkullMeta) is.getItemMeta();
		return new NMSHeadData(Bukkit.getOfflinePlayer(meta.getOwner()).getUniqueId(), getTexture(meta));
	}
	
	public static String getTexture(SkullMeta meta){
		Field field = null;
		try{
			field = meta.getClass().getDeclaredField("profile");
			field.setAccessible(true);
			GameProfile profile = (GameProfile) field.get(field);
			Iterator<Property> iterator = profile.getProperties().get("textures").iterator();
			while(iterator.hasNext()){
				Property p = iterator.next();
				if(p.getName().equals("textures"))
					return p.getValue();
			}
		}catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e){
			e.printStackTrace();
		}
		return null;
	}
}
