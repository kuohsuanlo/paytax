
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

    	config = this.getConfig();
    	config.addDefault("version","1.0.0");
    	config.addDefault("TAX_RATE",0.07);
    	config.options().copyDefaults(true);
    	saveConfig();
    	
    	TAX_RATE = config.getDouble("TAX_RATE");
    	
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        getLogger().info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    @SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender; 
        
        if(command.getLabel().equals("payoff")) {
                    
        	if (split.length == 2) {
                try {
                    String receiver_name = split[0];
                    double tranfered_amount= Double.parseDouble(split[1]);
                    double tranfered_amount_taxed= Double.parseDouble(split[1])*(1.00+TAX_RATE);
                    

                    if (econ.has(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed)){//��������
                    	EconomyResponse r_send = econ.withdrawPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                    	//sender.sendMessage("��a[PayTax] : Transcation success. You paied ��6"+receiver_name+" ��a"+econ.format(tranfered_amount)+", and��c being taxed "+Math.round(TAX_RATE*100)+"% :��a "+econ.format(tranfered_amount*TAX_RATE));
                    	//sender.sendMessage("��a[PayTax] : You now have "+ econ.format(econ.getBalance(player)));
                    	sender.sendMessage("��a��b���\: �A���\����I ���a��6"+receiver_name+" ��a"+econ.format(tranfered_amount)+",���B�~��c����"+ Math.round(TAX_RATE*100) +"% ����O��a "+econ.format(tranfered_amount*TAX_RATE));
                    	sender.sendMessage("��a�A�ثe���l�B��  : "+ econ.format(econ.getBalance(player)));
                    	//EconomyResponse r_receive = econ.depositPlayer(this.getServer().getOfflinePlayer(this.getServer().getPlayer(receiver_name).getUniqueId()), tranfered_amount);
                    	EconomyResponse r_receive = econ.depositPlayer( this.getServer().getOfflinePlayer(receiver_name), tranfered_amount);
                        
                    	
                		//sender.sendMessage("Debug  : "+ this.getServer().getOfflinePlayer(receiver_name));
                		//sender.sendMessage("Debug  : "+ this.getServer().getOfflinePlayer(receiver_name).getUniqueId());
                	    //sender.sendMessage("Debug  : "+ this.getServer().getPlayer(receiver_name));
                		//sender.sendMessage("Debug  : "+ this.getServer().getPlayer(receiver_name).getUniqueId());
                    	
                    	
                    	if(r_receive.transactionSuccess()  &&  r_send.transactionSuccess() ){
                        	if(this.getServer().getPlayer(receiver_name)!=null){
                        		//this.getServer().getPlayer(receiver_name).sendMessage("��a[PayTax] : Transcation success. You got ��6"+player.getName()+"'s��a money : "+econ.format(r_receive.amount));
                        		//this.getServer().getPlayer(receiver_name).sendMessage("��a[PayTax] : You now have "+ econ.format(r_receive.balance)+"��a after the transcation.");
                        		this.getServer().getPlayer(receiver_name).sendMessage("��a��b���\: �A����F�Ӧ� ���a��6"+player.getName()+" ��a�����B "+econ.format(r_receive.amount));
                        		this.getServer().getPlayer(receiver_name).sendMessage("��a�A�ثe�l�B�� "+ econ.format(r_receive.balance));
                        		EconomyResponse r_tax = econ.depositPlayer(this.getServer().getOfflinePlayer("tax"),  tranfered_amount*TAX_RATE);
                        		//EconomyResponse r_tax = econ.depositPlayer(this.getServer().getOfflinePlayer(this.getServer().getPlayer("tax").getUniqueId()),  tranfered_amount*TAX_RATE);
                             }
                        }
                        else {
                        	//basically this won't happened, because even the id never logs in, the payment is still valid, the money is still transfered to the mistyped account.
                        	//sender.sendMessage("��c[PayTax] : Transcation err: unknown situation.");
                        	sender.sendMessage("��c��b���~: ���������B���~");
                        	EconomyResponse r_withdraw = econ.depositPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                        	
                        }
                    }
                    else {
                    	//sender.sendMessage("��c[PayTax] : Transcation err: You can't afford the amount after taxing : "+econ.format(tranfered_amount_taxed)+".");
                    	sender.sendMessage("��c��b���~: �l�B������I ��b���B �[�W ����O,�@�@"+econ.format(tranfered_amount_taxed));
                    	//r_withdraw = econ.depositPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                    }
            		
            	
                    	
                    	
                    
                    
                    
                } catch (NumberFormatException ex) {
                	//player.sendMessage("��c[PayTax] : Transcation err: not a proper number of money");
                	player.sendMessage("��c��b���~: ��b���B  ���O���Ī��Ʀr");
                }
            }
        }
        else if(command.getLabel().equals("payon") || command.getLabel().equals("pay")  ||   command.getLabel().equals("paytax")  ){
        	if (split.length == 2) {
                try {
                    String receiver_name = split[0];
                    double tranfered_amount= Double.parseDouble(split[1]);
                    double tranfered_amount_taxed= Double.parseDouble(split[1])*(1.00+TAX_RATE);
                    
                    	if(this.getServer().getPlayer(receiver_name)==null){
                		//if(econ.hasAccount(this.getServer().getOfflinePlayer(receiver_name))){
                        	//sender.sendMessage("��c[PayTax] : Transcation err: Please reassure the receiver's ID. Is he or she online? Use /payoff to transfer money to offline players");
                        	//sender.sendMessage("��c��b���~: �нT�{���aID�O�_��J���~,�άO���b�u�W�C �p�G�Q�n��b�����u���a,�Шϥ�  ��e/payoff ID ���B  ��c(�ХJ�ӽT�{ID)");
                        	sender.sendMessage("��a[PayTax] : You now have "+ econ.format(econ.getBalance(player)));
                        	sender.sendMessage("��a�A�ثe���l�B��  : "+ econ.format(econ.getBalance(player)));
                    	}
                    	else{//ID�s�b
                            if (econ.has(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed)){//��������
                            	EconomyResponse r_send = econ.withdrawPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                            	sender.sendMessage("��a[PayTax] : Transcation success. You paied ��6"+receiver_name+" ��a"+econ.format(tranfered_amount)+", and��c being taxed "+Math.round(TAX_RATE*100)+"% :��a "+econ.format(tranfered_amount*TAX_RATE));
                            	sender.sendMessage("��a[PayTax] : You now have "+ econ.format(econ.getBalance(player)));
                            	//sender.sendMessage("��a��b���\: �A���\����I ���a��6"+receiver_name+" ��a"+econ.format(tranfered_amount)+",���B�~��c����"+ Math.round(TAX_RATE*100) +"% ����O��a "+econ.format(tranfered_amount*TAX_RATE));
                            	//sender.sendMessage("��a�A�ثe���l�B��  : "+ econ.format(econ.getBalance(player)));
                            	//EconomyResponse r_receive = econ.depositPlayer(this.getServer().getOfflinePlayer(this.getServer().getPlayer(receiver_name).getUniqueId()), tranfered_amount);
                            	EconomyResponse r_receive = econ.depositPlayer( this.getServer().getOfflinePlayer(receiver_name), tranfered_amount);
                                
                            	
                        		//sender.sendMessage("Debug  : "+ this.getServer().getOfflinePlayer(receiver_name));
                        		//sender.sendMessage("Debug  : "+ this.getServer().getOfflinePlayer(receiver_name).getUniqueId());
                        	    //sender.sendMessage("Debug  : "+ this.getServer().getPlayer(receiver_name));
                        		//sender.sendMessage("Debug  : "+ this.getServer().getPlayer(receiver_name).getUniqueId());
                            	
                            	
                            	if(r_receive.transactionSuccess()  &&  r_send.transactionSuccess() ){
                                	if(this.getServer().getPlayer(receiver_name)!=null){
                                		this.getServer().getPlayer(receiver_name).sendMessage("��a[PayTax] : Transcation success. You got ��6"+player.getName()+"'s��a money : "+econ.format(r_receive.amount));
                                		this.getServer().getPlayer(receiver_name).sendMessage("��a[PayTax] : You now have "+ econ.format(r_receive.balance)+"��a after the transcation.");
                                		//this.getServer().getPlayer(receiver_name).sendMessage("��a��b���\: �A����F�Ӧ� ���a��6"+player.getName()+" ��a�����B "+econ.format(r_receive.amount));
                                		//this.getServer().getPlayer(receiver_name).sendMessage("��a�A�ثe�l�B�� "+ econ.format(r_receive.balance));
                                		EconomyResponse r_tax = econ.depositPlayer(this.getServer().getOfflinePlayer("tax"),  tranfered_amount*TAX_RATE);
                                		//EconomyResponse r_tax = econ.depositPlayer(this.getServer().getOfflinePlayer(this.getServer().getPlayer("tax").getUniqueId()),  tranfered_amount*TAX_RATE);
                                     }
                                }
                                else {
                                	//basically this won't happened, because even the id never logs in, the payment is still valid, the money is still transfered to the mistyped account.
                                	sender.sendMessage("��c[PayTax] : Transcation err: unknown situation.");
                                	sender.sendMessage("��a[PayTax] : You now have "+ econ.format(econ.getBalance(player)));
                                	//sender.sendMessage("��c��b���~: ���������B���~");
                                	//sender.sendMessage("��a�A�ثe���l�B��  : "+ econ.format(econ.getBalance(player)));
                                	EconomyResponse r_withdraw = econ.depositPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                                	
                                }
                            }
                            else {
                            	sender.sendMessage("��c[PayTax] : Transcation err: You can't afford the amount after taxing : "+econ.format(tranfered_amount_taxed)+".");
                            	sender.sendMessage("��a[PayTax] : You now have "+ econ.format(econ.getBalance(player)));
                            	//sender.sendMessage("��c��b���~: �l�B������I ��b���B �[�W ����O,�@�@"+econ.format(tranfered_amount_taxed));
                            	//sender.sendMessage("��a�A�ثe���l�B��  : "+ econ.format(econ.getBalance(player)));
                            	//r_withdraw = econ.depositPlayer(this.getServer().getOfflinePlayer(player.getUniqueId()), tranfered_amount_taxed);
                            }
                    		
                    	}
                    	
                    	
                    
                    
                    
                } catch (NumberFormatException ex) {
                	//player.sendMessage("��c[PayTax] : Transcation err: not a proper number of money");
                	player.sendMessage("��c��b���~: ��b���B  ���O���Ī��Ʀr");
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
