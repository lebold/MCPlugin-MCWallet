package aplicable.mcplugin.mcwallet.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import aplicable.mcplugin.mcwallet.MCWallet;

public class SQLManager {
	private MCWallet master;
	private Connection connection;
	private String host;
	private String username;
	private String password;
	private String database;
	public SQLManager(MCWallet master){
		this.master=master;
		FileConfiguration config = this.master.getConfig();
		this.host = config.getString("host");
		this.username = config.getString("username");
		this.password = config.getString("password");
		this.database = config.getString("database");
		establishConnection();
		prepareDatabase();
	}
	private void establishConnection(){
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.connection=DriverManager.getConnection("jdbc:mysql://"+this.host+":3306/"+database,this.username,this.password);
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
		}catch(ClassNotFoundException e){
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
		}catch(IllegalAccessException e){
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
		}catch(InstantiationException e){
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
		}
	}
	public void closeConnection(){
		if(!this.connected())
			return;
		try {
			this.connection.close();
		} catch (SQLException e) {
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
		}
	}
	private void prepareDatabase(){
		if(!this.connected())
			return;
		try{
			Statement query = connection.createStatement();
			query.executeUpdate("CREATE TABLE IF NOT EXISTS MCWalletSQL (NAME VARCHAR(255),BALANCE INT)");
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWallet").severe("Error in Database prep.");
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}
	public boolean isPlayerLogged(String playername){
		if(!this.connected())
			return false;
		try{
			Statement query = connection.createStatement();
			ResultSet set = query.executeQuery("SELECT * FROM MCWalletSQL WHERE NAME='"+playername+"'");
			return set.first();
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWallet").severe("Error in locating Player.");
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
		return false;
	}
	public void logPlayer(String playername){
		if(!this.connected())
			return;
		try{
			Statement query = connection.createStatement();
			query.executeUpdate("INSERT INTO MCWalletSQL VALUES ('"+playername+"',0)");
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWallet").severe("Error in logging player.");
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}
	public void setBalance(String playername, int balance){
		if(!this.connected())
			return;
		try{
			Statement query = connection.createStatement();
			query.executeUpdate("UPDATE MCWalletSQL SET BALANCE="+balance+" WHERE NAME='"+playername+"'");
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWallet").severe("Error in updating balance.");
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}
	public int getBalance(String playername){
		if(!this.connected())
			return -1;
		try{
			Statement query = connection.createStatement();
			ResultSet set = query.executeQuery("SELECT BALANCE FROM MCWalletSQL WHERE NAME='"+playername+"'");
			set.next();
			int balance = set.getInt(1);
			return balance;
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWallet").severe("Error in retrieving balance.");
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");			
		}
		return -1;
	}
	
	public HashMap<String,Integer> fetchAllBalances(){
		if(!this.connected())
			return null;
		try{
			Statement query1 = connection.createStatement();
			Statement query2 = connection.createStatement();
			ResultSet nameSet = query1.executeQuery("SELECT NAME FROM MCWalletSQL");
			ResultSet balanceSet = query2.executeQuery("SELECT BALANCE FROM MCWalletSQL");
			HashMap<String,Integer> accountBalances = new HashMap<String,Integer>();
			while(nameSet.next()&&balanceSet.next()){
				accountBalances.put(nameSet.getString(1),balanceSet.getInt(1));
			}
			nameSet.close();
			balanceSet.close();
			return accountBalances;
		}catch(SQLException e){
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");
			Logger.getLogger("Minecraft.MCWallet").severe(e.getMessage());
			Logger.getLogger("Minecraft.MCWallet").severe("Error in retrieving balances.");
			Logger.getLogger("Minecraft.MCWallet").severe("~~~~~~~~~~~~~~~~~~~~~~~");		
		}
		return null;
	}
	private boolean connected(){
		if(this.connection==null){
			Logger.getLogger("Minecraft.MCWallet").severe("Could not connect to database.");
			return false;
		}
		else
			return true;
	}
}
