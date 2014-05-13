package aplicable.mcplugin.mcwallet.commands;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import aplicable.mcplugin.mcwallet.Bank;
import aplicable.mcplugin.mcwallet.MCWallet;

public class CommandMoney {
	MCWallet master;
	Bank bank;
	public CommandMoney(MCWallet master, Bank bank){
		this.master=master;
		this.bank=bank;
	}
	public void issue(CommandSender sender, Command cmd, String label, String[] args){
		if(args.length==0){
			if(sender.hasPermission("money.check"))
				this.statement(sender,cmd,label,args);
			else
				this.insufficientPermissions(sender);
		}
		else if(args.length==1){
			if(args[0].equalsIgnoreCase("rank")){
				if(sender.hasPermission("money.checkrank")){
					String[] pushArgs = {args[0],sender.getName()};
					this.checkRank(sender,cmd,label,pushArgs);
				}
				else
					this.insufficientPermissions(sender);
			}
			else if(args[0].equalsIgnoreCase("help")){
				if(sender.hasPermission("money.help")){
					this.correctFormat(sender);
				}
				else
					this.insufficientPermissions(sender);
			}
			else{
				if(sender.hasPermission("money.checkother"))
					this.statementOther(sender, cmd, label, args);
				else
					this.insufficientPermissions(sender);
			}
		}
		else if(args.length==2){
			if(args[0].equalsIgnoreCase("rank")){
				if(args[1].equalsIgnoreCase("top")){
					if(sender.hasPermission("money.checkranktop"))
						this.displayTop(sender,cmd,label,args);
					else
						this.insufficientPermissions(sender);
				}
				else{
					if(sender.hasPermission("money.checkrankother"))
						this.checkRank(sender,cmd,label,args);
					else
						this.insufficientPermissions(sender);
				}
			}
			else
				this.correctFormat(sender);
		}
		else if(args[0].equalsIgnoreCase("add")){
			if(sender.hasPermission("money.add"))
				this.add(sender,cmd,label,args);
			else
				this.insufficientPermissions(sender);
		}
		else if(args[0].equalsIgnoreCase("set")){
			if(sender.hasPermission("money.set"))
				this.set(sender,cmd,label,args);
			else
				this.insufficientPermissions(sender);
		}
		else if(args[0].equalsIgnoreCase("remove")){
			if(sender.hasPermission("money.remove"))
				this.remove(sender,cmd,label,args);
			else
				this.insufficientPermissions(sender);
		}
		else if(args[0].equalsIgnoreCase("give")){
			if(sender.hasPermission("money.give"))
				this.give(sender,cmd,label,args);
			else
				this.insufficientPermissions(sender);
		}
		else{
			this.correctFormat(sender);
		}
	}

	private void insufficientPermissions(CommandSender sender){
		sender.sendMessage(ChatColor.RED + "You do not have sufficient permissions to issue this command.");
	}

	private void displayTop(CommandSender sender, Command cmd, String label, String[] args){
		HashMap<String,Integer> accounts = this.master.fetchAllAccounts();
		ArrayList<String> topPlayers = new ArrayList<String>();
		for(String name:accounts.keySet()){
			for(int n=0;n<5;n++){
				if(n>=topPlayers.size()){
					topPlayers.add(name);
					break;
				}
				else if(accounts.get(name)>accounts.get(topPlayers.get(n))){
					topPlayers.add(n, name);
					if(topPlayers.size()>5)
						topPlayers.remove(5);
					break;
				}
			}
		}
		sender.sendMessage(ChatColor.UNDERLINE + "Top Players:");
		for(int n=0;n<topPlayers.size();n++){
			int length = topPlayers.get(n).length();
			String strBreak = "";
			for(int i=20;i>=length;i--){
				strBreak += " ";
			}
			sender.sendMessage(ChatColor.WHITE + "" + (n+1) + ". " + ChatColor.AQUA + topPlayers.get(n) + ChatColor.GOLD + strBreak + accounts.get(topPlayers.get(n)) + ChatColor.WHITE + "coins.");
		}
	}

	private void checkRank(CommandSender sender, Command cmd, String label, String[] args){
		Player player = this.master.fetchPlayer(args[1]);
		String playerName = args[1];
		int balance = -1;
		if(player==null){
			balance = this.master.fetchBalanceFromStorage(args[1]);
		}
		else
			balance = this.bank.statement(player);
		if(balance==-1){
			sender.sendMessage(ChatColor.YELLOW + "The specified player could not be found.");
			return;
		}
		int rank = 1;
		int total = 0;
		HashMap<String,Integer> accounts = this.master.fetchAllAccounts();
		for(int i:accounts.values()){
			if(i>balance)
				rank++;
			total++;
		}
		if(balance==1)
			sender.sendMessage(ChatColor.AQUA + playerName + ChatColor.WHITE + "'s account balance is: " + ChatColor.GOLD + "" + balance + ChatColor.WHITE + " coin.");
		else
			sender.sendMessage(ChatColor.AQUA + playerName + ChatColor.WHITE + "'s account balance is: " + ChatColor.GOLD + "" + balance + ChatColor.WHITE + " coins.");
		sender.sendMessage(ChatColor.AQUA + playerName + ChatColor.WHITE + "'s account is ranked " + ChatColor.GOLD + "" + rank + " " + ChatColor.WHITE + "out of " + ChatColor.GOLD + "" + total + ChatColor.WHITE + " players.");
	}

	public void statement(CommandSender sender, Command cmd, String label, String[] args){
		Player player = (Player)sender;
		int balance = this.bank.statement(player);
		if(balance<0){
			sender.sendMessage(ChatColor.YELLOW + "You are not in our records as a player.");
			sender.sendMessage(ChatColor.YELLOW + "Please consult with an operator.");
		}
		else if(balance==1)
			sender.sendMessage(ChatColor.WHITE + "Your balance is: " + ChatColor.GOLD + "" + balance + ChatColor.WHITE + " coin.");
		else
			sender.sendMessage(ChatColor.WHITE + "Your balance is: " + ChatColor.GOLD + "" + balance + ChatColor.WHITE + " coins.");
	}

	public void statementOther(CommandSender sender, Command cmd, String label, String[] args){
		String playername = args[0];
		Player p = this.master.fetchPlayer(playername);
		int balance = this.bank.statement(p);
		if(balance<0){
			sender.sendMessage(ChatColor.YELLOW + "The player specified could not be found.");
		}
		else if(balance==1)
			sender.sendMessage(ChatColor.AQUA + args[0] + ChatColor.WHITE + "'s balance is: " + ChatColor.GOLD + "" + balance + ChatColor.WHITE + " coin.");
		else
			sender.sendMessage(ChatColor.AQUA + args[0] + ChatColor.WHITE + "'s balance is: " + ChatColor.GOLD + "" + balance + ChatColor.WHITE + " coins.");
	}

	public void add(CommandSender sender, Command cmd, String label, String[] args){
		if(!checkParams(sender,args,"add"))
			return;
		Player player = this.master.fetchPlayer(args[1]);
		int amount = Integer.parseInt(args[2]);
		if(bank.deposit(player, amount)){
			if(amount==1)
				sender.sendMessage(ChatColor.GOLD + "" + amount + ChatColor.WHITE + " coin has been deposited into " + ChatColor.AQUA + player.getName() + ChatColor.WHITE + "'s account.");
			else
				sender.sendMessage(ChatColor.GOLD + "" + amount + ChatColor.WHITE + " coins have been deposited into " + ChatColor.AQUA + player.getName() + ChatColor.WHITE + "'s account.");
		}
		else{
			sender.sendMessage(ChatColor.RED + "The transaction failed.");
		}
		this.master.updatePlayer(player);
	}

	public void set(CommandSender sender, Command cmd, String label, String[] args){
		if(!checkParams(sender,args,"set"))
			return;
		Player player = this.master.fetchPlayer(args[1]);
		int amount = Integer.parseInt(args[2]);
		if(bank.setBalance(player, amount)){
			if(amount==1)
				sender.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.WHITE + "'s balance has been set to: " + ChatColor.GOLD + "" + amount + ChatColor.WHITE + " coin.");
			else
				sender.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.WHITE + "'s balance has been set to: " + ChatColor.GOLD + "" + amount + ChatColor.WHITE + " coins.");
		}
		else{
			sender.sendMessage(ChatColor.RED + "The transaction failed.");
		}
		this.master.updatePlayer(player);
	}

	public void give(CommandSender sender, Command cmd, String label, String[] args){
		if(!checkParams(sender,args,"give"))
			return;
		Player giver = (Player)sender;
		Player receiver = this.master.fetchPlayer(args[1]);
		if(giver.getName().equalsIgnoreCase(receiver.getName())){
			sender.sendMessage(ChatColor.YELLOW + "You cannot engage in a transaction with yourself.");
			return;
		}
		int amount = Integer.parseInt(args[2]);
		if(bank.transaction(giver, receiver, amount)){
			if(amount==1){
				giver.sendMessage(ChatColor.WHITE + "You have sent " + ChatColor.AQUA + receiver.getName() + ChatColor.GOLD + " " + amount + ChatColor.WHITE + " coin.");
				receiver.sendMessage(ChatColor.AQUA + giver.getName() + ChatColor.WHITE + " has given you " + ChatColor.GOLD + "" + amount + ChatColor.WHITE + " coin.");
			}
			else{
				giver.sendMessage(ChatColor.WHITE + "You have sent " + ChatColor.AQUA + receiver.getName() + ChatColor.GOLD + " " + amount + ChatColor.WHITE + " coins.");
				receiver.sendMessage(ChatColor.AQUA + giver.getName() + ChatColor.WHITE + " has given you " + ChatColor.GOLD + "" + amount + ChatColor.WHITE + " coins.");
			}
		}
		else{
			sender.sendMessage(ChatColor.RED + "The transaction failed.");
		}
		this.master.updatePlayer(giver);
		this.master.updatePlayer(receiver);
	}

	public void remove(CommandSender sender, Command cmd, String label, String[] args){
		if(!checkParams(sender,args,"remove"))
			return;
		Player player = this.master.fetchPlayer(args[1]);
		int amount = Integer.parseInt(args[2]);
		if(bank.withdraw(player, amount)){
			if(amount==1)
				sender.sendMessage(ChatColor.GOLD + "" + amount + ChatColor.WHITE + " coin has been withdrawn from " + ChatColor.AQUA + player.getName() + ChatColor.WHITE + "'s account.");
			else
				sender.sendMessage(ChatColor.GOLD  + "" + amount + ChatColor.WHITE + " coins have been withdrawn from " + ChatColor.AQUA + player.getName() + ChatColor.WHITE + "'s account.");
		}
		else{
			sender.sendMessage(ChatColor.RED + "The transaction failed.");
		}
		this.master.updatePlayer(player);
	}

	private boolean checkParams(CommandSender sender, String[] args, String id){
		try{
			int i = Integer.parseInt(args[2]);
			if(i<=0 && !id.equalsIgnoreCase("set")){
				sender.sendMessage(ChatColor.YELLOW + "The minimum amount of money allowed for this transaction is "+ChatColor.GOLD+"1 coin"+ChatColor.WHITE+".");
				return false;
			}
			else if(i<0 && id.equalsIgnoreCase("set")){
				sender.sendMessage(ChatColor.YELLOW + "The minimum possible balance for a player is "+ChatColor.GOLD+"0 coins"+ChatColor.WHITE+".");
				return false;
			}
		}catch(Exception e){
			sender.sendMessage(ChatColor.RED + "The correct format for this command is:");
			sender.sendMessage(ChatColor.RED + "/money " + id + " <playername> <amount>");
			return false;
		}
		Player player = this.master.fetchPlayer(args[1]);
		if(player == null){
			sender.sendMessage(ChatColor.YELLOW + "The player you specified could not be found.");
			return false;
		}
		return true;
	}
	private void correctFormat(CommandSender sender){
		sender.sendMessage(ChatColor.RED + "The correct format for this command is:");
		sender.sendMessage(ChatColor.RED + "/money <add/set/remove/give> <playername> <amount>");
		sender.sendMessage(ChatColor.RED + "/money [playername]");
		sender.sendMessage(ChatColor.RED + "/money rank [playername]");
		sender.sendMessage(ChatColor.RED + "/money help");
	}
}
