package aplicable.mcplugin.mcwallet;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import aplicable.mcplugin.mcwallet.commands.CommandMoney;
import aplicable.mcplugin.mcwallet.listeners.PlayerListener;
import aplicable.mcplugin.mcwallet.sql.SQLManager;

public class MCWallet extends JavaPlugin {
	private final int PAY_AMOUNT = 5;
	private final int PAY_RATE = 300 * 20; // seconds * ticks/second

	private ArrayList<Player> playerList;
	private Bank bank;
	private CommandMoney commandMoney;
	private SQLManager sqlmanager;
	private int payTaskID;

	public void onEnable(){
		this.sqlmanager = new SQLManager(this);
		this.playerList = new ArrayList<Player>();
		this.bank = new Bank();
		this.commandMoney = new CommandMoney(this,this.bank);
		for(Player player:Bukkit.getServer().getOnlinePlayers()){
			this.welcome(player);
		}
		this.registerListeners();
		this.payTaskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				payPlayers();
			}
		}, this.PAY_RATE, this.PAY_RATE);
	}
	public void onDisable(){
		if(this.sqlmanager==null)
			this.sqlmanager = new SQLManager(this);
		for(int n=this.playerList.size()-1;n>=0;n--)
			this.dismiss(this.playerList.get(n));
		this.sqlmanager.closeConnection();
		this.getServer().getScheduler().cancelTask(this.payTaskID);
	}
	public void payPlayers(){
		for(Player player:this.playerList){
			this.bank.deposit(player, this.PAY_AMOUNT);
			this.updatePlayer(player);
		}
	}
	public boolean hasPlayer(Player p){
		return this.playerList.contains(p);
	}
	public Player fetchPlayer(String playerName){
		for(Player player:this.playerList){
			if(player.getName().equalsIgnoreCase(playerName)){
				return player;
			}
		}
		return null;
	}
	public void updatePlayer(Player player){
		int balance = this.bank.statement(player);
		this.sqlmanager.setBalance(player.getName(), balance);
	}
	public int fetchBalanceFromStorage(String playerName){
		return this.sqlmanager.getBalance(playerName);
	}
	public HashMap<String,Integer> fetchAllAccounts(){
		return this.sqlmanager.fetchAllBalances();
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("money")){
			this.commandMoney.issue(sender,cmd,label,args);
		}
		return false;
	}
	public void registerListeners(){
		PluginManager pm = super.getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this),this);
	}
	public void welcome(Player player){
		if(player.isWhitelisted()){
			if(!this.playerList.contains(player))
				this.playerList.add(player);
			if(this.sqlmanager.isPlayerLogged(player.getName())){
				int balance = this.sqlmanager.getBalance(player.getName());
				this.bank.createAccount(player,balance);
			}
			else{
				this.sqlmanager.logPlayer(player.getName());
				this.bank.createAccount(player);
			}
		}
	}
	public void dismiss(Player player){
		if(player.isWhitelisted()){
			this.playerList.remove(player);
			if(this.bank.statement(player)>=0)
				this.sqlmanager.setBalance(player.getName(),this.bank.statement(player));
			this.bank.recindAccount(player);
		}
	}
}
