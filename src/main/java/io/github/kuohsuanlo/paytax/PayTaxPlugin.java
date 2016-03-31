
package io.github.kuohsuanlo.paytax;


import java.io.File;
import java.util.logging.Logger;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
/**
 * Sample plugin for Bukkit
 *
 * @author Dinnerbone
 */
public class PayTaxPlugin extends JavaPlugin {
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    
    private static final Logger log = Logger.getLogger("Minecraft");
    private double TAX_RATE = 0.07;
    public static Economy econ = null;
    private FileConfiguration config;
    @Override
    public void onDisable() {
        // TODO: Place any custom disable code here
        // NOTE: All registered events are automatically unregistered when a plugin is disabled
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register our events
        PluginManager pm = getServer().getPluginManager();

    	new File("./plugins/PayTax").mkdirs();
    	config = this.getConfig();
    	
    	//config.createSection("#The following time period and max_untouched_chunk tolerance before it gets restored");
    	//config.createSection("#needs a server restart to change.");
    	config.addDefault("version","1.0.0");
    	config.addDefault("TAX_RATE",0.07);
    	config.options().copyDefaults(true);
    	saveConfig();
    	
    	TAX_RATE = config.getDouble("TAX_RATE");
    	
    	
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        getLogger().info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        
        if(command.getLabel().equals("paytax")  ||  command.getLabel().equals("pay")) {
            
            if (split.length == 2) {
                try {
                    String receiver_name = split[0];
                    double tranfered_amount= Double.parseDouble(split[1]);
                    double tranfered_amount_taxed= tranfered_amount*(1.00+TAX_RATE);
                    
                    EconomyResponse r_withdraw = econ.withdrawPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                    
                    if (r_withdraw.transactionSuccess()){
                    	sender.sendMessage("§transcation success: You paied §6"+receiver_name+" §a"+econ.format(tranfered_amount)+",and§c being taxed "+Math.round(TAX_RATE*100)+"% of the amount : §a "+econ.format(tranfered_amount*TAX_RATE));
                    	sender.sendMessage("§aYou have balance  : "+ econ.format(econ.getBalance(player)));
                    	//sender.sendMessage("§a轉帳成功: 你成功的支付 玩家§6"+receiver_name+" §a"+econ.format(tranfered_amount)+",並額外§c扣除7% 手續費§a "+econ.format(tranfered_amount*0.07));
                    	//sender.sendMessage("§a你目前的餘額為  : "+ econ.format(econ.getBalance(player)));
                    	
                    	EconomyResponse r_receive = econ.depositPlayer(this.getServer().getOfflinePlayer(this.getServer().getPlayer(receiver_name).getUniqueId()), tranfered_amount);
                        
                    	if(r_receive.transactionSuccess()){
                        	if(this.getServer().getPlayer(receiver_name)!=null){
                        		this.getServer().getPlayer(receiver_name).sendMessage("§atranscation success: You got §6"+player.getName()+" §a money : "+econ.format(r_receive.amount));
                        		this.getServer().getPlayer(receiver_name).sendMessage("§aYou have balance "+ econ.format(r_receive.balance)+"§a after transcation.");
                        		//this.getServer().getPlayer(receiver_name).sendMessage("§a轉帳成功: 你收到了來自 玩家§6"+player.getName()+" §a的金額 "+econ.format(r_receive.amount));
                        		//this.getServer().getPlayer(receiver_name).sendMessage("§a你目前餘額為 "+ econ.format(r_receive.balance));
                        		EconomyResponse r_tax = econ.depositPlayer(this.getServer().getOfflinePlayer("tax"),  tranfered_amount*0.07);
                        		//EconomyResponse r_tax = econ.depositPlayer(this.getServer().getOfflinePlayer(this.getServer().getPlayer("tax").getUniqueId()),  tranfered_amount*0.07);
                             }
                        }
                        else{
                        	//basically this won't happened, because even the id never logs in, the payment is still valid, the money is still transfered to the mistyped account.
                        	//sender.sendMessage("§c轉帳錯誤: 請確認金額後重新轉帳");
                        	sender.sendMessage("§ctranscation err: Please reassure you transcation amount.");
                        	r_withdraw = econ.depositPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                        }
                    }
                    else {
                    	sender.sendMessage("§ctranscation err: You can't afford the amount after taxing : "+econ.format(tranfered_amount_taxed)+".");
                    	//sender.sendMessage("§c轉帳錯誤: 餘額不足支付 轉帳金額 加上 手續費,一共"+econ.format(tranfered_amount_taxed));
                        //sender.sendMessage(String.format("An error occured: %s", r_withdraw.errorMessage));
                    }
                    ;
                } catch (NumberFormatException ex) {
                	player.sendMessage("§ctranscation err: You can't afford the amount after being taxed.");
                	//player.sendMessage("§c轉帳錯誤: 餘額不足支付  轉帳金額   加上  手續費");
                }
            }
        }

		return false;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


}
