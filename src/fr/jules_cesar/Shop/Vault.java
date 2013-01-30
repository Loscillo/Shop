package fr.jules_cesar.Shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Vault extends JavaPlugin {
	
	public static Permission perms = null;
	public static Economy economy = null;
	public static Plugin plugin = null;
	
	public static void load(Plugin p){
		plugin = p;
	}

    public static boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    public static boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}