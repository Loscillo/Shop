package fr.jules_cesar.Shop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener{
	
	private ShopCommand CommandExecutor = new ShopCommand();
	private static String url = null;
	private static String user = null;
	private static String pass = null;
	
	
	@Override
	public void onEnable(){
		boolean erreur = false;
		getServer().getPluginManager().registerEvents(this, this);
		
		// Chargement plugin Žconomie et permission
		Vault.load(this);
		if(!Vault.setupPermissions()){
			erreur = true;
			System.out.println("[SHOP] Erreur : Pas de plugin de permission disponible !");
		}
		if(!Vault.setupEconomy()){
			erreur = true;
			System.out.println("[SHOP] Erreur : Pas de plugin d'economie disponible !");
		}
		
		// Chargement des informations MySQL
		if(!this.getDataFolder().exists()) this.getDataFolder().mkdir();
		this.saveDefaultConfig();
		FileConfiguration config = this.getConfig();
		user = config.getString("user");
		pass = config.getString("pass");
		String server = config.getString("server");
		String port = config.getString("port");
		String database = config.getString("database");
		if(user == null || pass == null || server == null || port == null || database == null){
			erreur = true;
			System.out.println("[SHOP] Erreur : Le fichier de configuration ne contient pas les informations utiles");
		}
		url = "jdbc:mysql://"+server+":"+port+"/"+database;
		try{
			Class.forName("com.mysql.jdbc.Driver");
		    Connection conn = DriverManager.getConnection(url, user, pass);
		    conn.close();
		    System.out.println("[SHOP] Connexion a la base de donnees reussie");
		}
		catch(SQLException e){
			erreur = true;
			System.out.println("[SHOP] Erreur : Connexion a la base de donnees impossible");
		}
		catch (ClassNotFoundException e) {
			erreur = true;
			System.out.println("[SHOP] Erreur : Le driver MySQL n'a pas ete detecte");
		}
		
		if(!erreur){
			// Chargement des commandes
			ShopCommand.load(this);
			getCommand("shop").setExecutor(CommandExecutor);

			// DŽmarrage du calcul
			Calcul.load(this);
			new Calcul();
		}
		else getServer().getPluginManager().disablePlugin(this);
	}
	
	public void onDisable(){
		// Arret du calcul
		getServer().getScheduler().cancelTasks(this);
	}

	public static String getUrl(){
		return url;
	}
	
	public static String getUser(){
		return user;
	}
	
	public static String getPass(){
		return pass;
	}
}
