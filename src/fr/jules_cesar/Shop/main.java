package fr.jules_cesar.Shop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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
		
		// Chargement plugin économie et permission
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

			// Démarrage du calcul
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

	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST){
				Sign panneau = (Sign)(e.getClickedBlock().getState());
				if(panneau.getLine(0).equalsIgnoreCase("[shop]")){
					String id = panneau.getLine(1);
					String data = panneau.getLine(2);
					id = id.substring(id.indexOf(':') + 2);
					if(data.equalsIgnoreCase("pas de data")) data = "0";
					else data = data.substring(data.indexOf(':') + 2);
					information(e.getPlayer(), id, data);
				}
			}
		}
	}
	
	private boolean information(Player joueur, String id, String data){
		if(ShopCommand.estunNombre(id) && ShopCommand.estunNombre(data)){
			double prix = 0;
			int stock = 0;
			try {
				Class.forName("com.mysql.jdbc.Driver");
				 
				Connection conn = DriverManager.getConnection(main.getUrl(), main.getUser(), main.getPass());
			      
			      Statement state = conn.createStatement();
			      //L'objet ResultSet contient le résultat de la requête SQL
			      ResultSet result = state.executeQuery("SELECT prix, stock FROM shop WHERE id = " + id + " AND data = " + data);
			      while(result.next()){
			    	  prix = result.getDouble(1);
			    	  stock = result.getInt(2);
			      }
			      state.close();
			      conn.close();
			      
			}
			catch (ClassNotFoundException e) { e.printStackTrace(); }
			catch (SQLException e) { e.printStackTrace(); }
			if(!(prix == 0 && stock == 0)){
				ItemStack info = new ItemStack(Integer.parseInt(id), 0, Byte.parseByte(data));
				joueur.sendMessage(ChatColor.GOLD + "----- " + info.getType().toString() + (data.equalsIgnoreCase("0")?"":":" + data) + " -----");
				joueur.sendMessage(ChatColor.GOLD + "Prix : " + ShopCommand.formatDouble(prix, 2) + " EIG");
				joueur.sendMessage(ChatColor.RED + "Prix sans stock : " + ShopCommand.formatDouble(prix * 1.1, 2) + " EIG");
				joueur.sendMessage(ChatColor.GOLD + "Stock : " + stock);
				joueur.sendMessage(Shop.shop_message + "Commande pour acheter : /shop acheter " + id + (data.equalsIgnoreCase("0")?"":" " + data) + " [quantité]");
				joueur.sendMessage(Shop.shop_message + "Commande pour vendre : /shop vendre " + id + (data.equalsIgnoreCase("0")?"":" " + data) + " [quantité]");
			}
			else{
				joueur.sendMessage(Shop.shop_message + ChatColor.RED + "Ce shop n'est pas encore configuré");
			}
		}
		else{
			joueur.sendMessage(ChatColor.GOLD + "[SHOP] " + ChatColor.RED + "L'id et la data doivent être des nombres");
		}
		return true;
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e){
		if(e.getLine(0).equalsIgnoreCase("[shop]") && !Vault.perms.has(e.getPlayer().getWorld(), e.getPlayer().getName(), "shop.modification"))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onBreakBlock(BlockBreakEvent e){
		if(e.getBlock().getType() == Material.WALL_SIGN || e.getBlock().getType() == Material.SIGN_POST){
			Sign panneau = (Sign)(e.getBlock().getState());
			if(panneau.getLine(0).equalsIgnoreCase("[shop]") && !Vault.perms.has(e.getPlayer().getWorld(), e.getPlayer().getName(), "shop.modification"))
				e.setCancelled(true);
		}
	}
}
