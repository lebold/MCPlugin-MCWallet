package aplicable.mcplugin.mcwallet;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class Bank {
	private HashMap<Player,Integer> accounts;

	public Bank(){
		this.accounts = new HashMap<Player,Integer>();
	}

	public void createAccount(Player player){
		this.createAccount(player,0);
	}
	public void createAccount(Player player, int amount){
		this.accounts.put(player, amount);
	}
	public void recindAccount(Player player){
		if(this.accounts.containsKey(player))
			this.accounts.remove(player);
	}

	public int statement(Player player){
		if(player==null)
			return -1;
		if(!this.accounts.containsKey(player))
			return -1;
		else
			return this.accounts.get(player);
	}

	public boolean deposit(Player player, int amount){
		if(!this.accounts.containsKey(player))
			return false;
		int accountBalance = this.accounts.get(player);
		accountBalance += amount;
		this.accounts.put(player,accountBalance);
		return true;
	}

	public boolean withdraw(Player player, int amount){
		if(!this.accounts.containsKey(player))
			return false;
		int accountBalance = this.accounts.get(player);
		accountBalance -= amount;
		if(accountBalance < 0)
			return false;
		this.accounts.put(player,accountBalance);
		return true;
	}

	public boolean setBalance(Player player, int amount){
		if(!this.accounts.containsKey(player))
			return false;
		this.accounts.put(player,amount);
		return true;
	}

	public boolean transaction(Player giver, Player receiver, int amount){
		if(!this.accounts.containsKey(giver)||!this.accounts.containsKey(receiver))
			return false;
		int giverBalance = this.accounts.get(giver);
		int receiverBalance = this.accounts.get(receiver);
		giverBalance -= amount;
		if(giverBalance < 0)
			return false;
		receiverBalance += amount;
		this.accounts.put(giver,giverBalance);
		this.accounts.put(receiver,receiverBalance);
		return true;
	}
}
