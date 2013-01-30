package fr.jules_cesar.Shop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Calcul {
	
	private static Plugin plugin;
	
	public static void load(Plugin p){
		plugin = p;
	}
	
	BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
	    @Override  
	    public void run() {
	    	try {
				Class.forName("com.mysql.jdbc.Driver");
				Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
			      
				Statement state = conn.createStatement();
				state.executeUpdate("UPDATE shop SET prix = prix + (prix * difference / 100), difference = 0 WHERE prix >= 1");
				state.executeUpdate("UPDATE shop SET prix = prix + (prix * difference / 1000), difference = 0 WHERE prix < 1");
				state.executeUpdate("UPDATE shop SET prix = 1 WHERE prix <= 0");
				state.close();
				conn.close();
			}
			catch (ClassNotFoundException e) { e.printStackTrace(); }
			catch (SQLException e) { e.printStackTrace(); }
	    }
	}, 6000L, 6000L);
}
