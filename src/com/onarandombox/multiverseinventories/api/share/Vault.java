package com.onarandombox.multiverseinventories.api.share;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class Vault {
	private static Economy economy = null;
	
	public static void onEnable(){
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
	}
	
	public static double getBalance(Player player){
		if(economy != null)
			return economy.getBalance(player);
		else
			throw new NullPointerException("Missing Vault");
	}
	
	public static void setBalance(Player player, double bal){
		if(economy != null){
			economy.withdrawPlayer(player, economy.getBalance(player));
			economy.depositPlayer(player, bal);
		}else
			throw new NullPointerException("Missing Vault");
	}
}
