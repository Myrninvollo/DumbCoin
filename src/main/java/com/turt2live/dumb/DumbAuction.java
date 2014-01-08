package com.turt2live.dumb;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DumbAuction extends JavaPlugin {

    public static DumbAuction p;
    public static Economy economy;

    private List<String> toggles = new ArrayList<String>();
    private AuctionManager auctions;
    private List<String> ignoreBroadcast = new ArrayList<String>();

    @Override
    public void onEnable() {
        p = this;
        saveDefaultConfig();
        if (!setupEconomy()) {
            getLogger().severe("COULD NOT SETUP VAULT ECONOMY! Disabling while you fix that.");
            getServer().getPluginManager().disablePlugin(this);
        }

        auctions = new AuctionManager();
        toggles.add("toggle");
        toggles.add("stfu");
        toggles.add("silence");
        toggles.add("ignore");
        toggles.add("quiet");

        ignoreBroadcast = getConfig().getStringList("ignore-broadcast");
        if (ignoreBroadcast == null) {
            ignoreBroadcast = new ArrayList<String>();
        }

        // Easter eggs, for DBO
        String easterEgg = ":o";
        String fucknut = "drtshock";
        String drtlovesme = "lolnope.";
        String iKnowYouCanReadThisCauseYouAreReviewingIt = fucknut + " " + drtlovesme + " " + easterEgg;
    }

    @Override
    public void onDisable() {
        p = null;
        auctions.stop(); // Returns items
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("auction")) {
            if (!sender.hasPermission("dumbauction.auction")) {
                sendMessage(sender, ChatColor.RED + "No permission.");
            } else {
                if (sender instanceof Player) {
                    if (args.length < 1) {
                        sendMessage(sender, ChatColor.RED + "Incorrect syntax. Did you mean " + ChatColor.YELLOW + "/auc <start | info | showqueue | cancel | toggle | bid>" + ChatColor.RED + "?");
                    } else {
                        if (args[0].equalsIgnoreCase("start")) {
                            if (ignoreBroadcast.contains(sender.getName())) {
                                sendMessage(sender, ChatColor.RED + "You must be listening to auctions to do that.");
                                return true;
                            }
                            if (args.length < 2) {
                                showAucHelp(sender, args.length);
                            } else {
                                Player player = (Player) sender;
                                try {
                                    ItemStack hand = player.getItemInHand();
                                    if (hand == null || hand.getType() == Material.AIR) {
                                        sendMessage(sender, ChatColor.RED + "You are not holding anything to sell!");
                                        return true;
                                    }
                                    int found = 0;
                                    List<ItemStack> valid = new ArrayList<ItemStack>();
                                    for (ItemStack stack : player.getInventory().getContents()) {
                                        if (stack != null && stack.isSimilar(hand)) {
                                            found += stack.getAmount();
                                            valid.add(stack);
                                        }
                                    }
                                    double startPrice = Double.parseDouble(args[1]);
                                    double increment = Double.parseDouble(args[2]);
                                    long time = args.length > 3 ? Long.parseLong(args[3]) : getConfig().getLong("default-time-seconds", 30);
                                    int amount = args.length > 4 ? (args[4].equalsIgnoreCase("*") ? found : Integer.parseInt(args[4])) : hand.getAmount();

                                    // Check one : Time limit
                                    if (time >= getConfig().getLong("min-auction-time", 10)) {
                                        if (!player.hasPermission("dumbauction.admin") && time > getConfig().getLong("max-auction-time", 60)) {
                                            sendMessage(sender, ChatColor.RED + "Time too large!");
                                            return true;
                                        }
                                    } else {
                                        sendMessage(sender, ChatColor.RED + "Time too small!");
                                        return true;
                                    }

                                    // Check two : Start price
                                    if (time >= getConfig().getLong("min-start-cost", 10)) {
                                        if (!player.hasPermission("dumbauction.admin") && time > getConfig().getLong("max-start-cost", 20000)) {
                                            sendMessage(sender, ChatColor.RED + "Cost too large!");
                                            return true;
                                        }
                                    } else {
                                        sendMessage(sender, ChatColor.RED + "Cost too small!");
                                        return true;
                                    }

                                    // Check three : Bid price
                                    if (time >= getConfig().getLong("min-bid-cost", 10)) {
                                        if (!player.hasPermission("dumbauction.admin") && time > getConfig().getLong("max-bid-cost", 20000)) {
                                            sendMessage(sender, ChatColor.RED + "Bid increment too large!");
                                            return true;
                                        }
                                    } else {
                                        sendMessage(sender, ChatColor.RED + "Bid increment too small!");
                                        return true;
                                    }

                                    // Check four : Enough items?
                                    if (found < amount) {
                                        sendMessage(sender, ChatColor.RED + "You do not have enough of that item!");
                                        return true;
                                    }

                                    // Create a list of proper items (the one's we're taking)
                                    List<ItemStack> proper = new ArrayList<ItemStack>();
                                    int taken = 0;
                                    for (ItemStack itemStack : valid) {
                                        int newTaken = taken + itemStack.getAmount();
                                        ItemStack stack = itemStack;
                                        if (newTaken > amount) {
                                            stack = itemStack.clone();
                                            stack.setAmount(amount - taken);
                                            newTaken = amount;
                                        }
                                        proper.add(stack);
                                        taken = newTaken;
                                        if (taken > amount) break;
                                    }

                                    // Create auction
                                    Auction auction = new Auction(player.getName(), increment, startPrice, time, proper);

                                    if (!DumbAuction.economy.has(player.getName(), getConfig().getDouble("tax", 5))) {
                                        sendMessage(sender, ChatColor.RED + "You cannot afford the tax!");
                                        return true;
                                    }

                                    // Do they already have an auction?
                                    for (Auction auction1 : auctions.getAuctions()) {
                                        if (auction1.getSeller().equalsIgnoreCase(sender.getName())) {
                                            sendMessage(sender, ChatColor.RED + "You already have an auction in queue!");
                                            return true;
                                        }
                                    }
                                    if (auctions.getActiveAuction() != null && auctions.getActiveAuction().getSeller().equalsIgnoreCase(sender.getName())) {
                                        sendMessage(sender, ChatColor.RED + "You already have an auction in queue!");
                                        return true;
                                    }

                                    // Register auction
                                    if (auctions.addAuction(auction)) {
                                        int removed = 0;
                                        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                                            ItemStack item = player.getInventory().getItem(slot);
                                            if (item != null) {
                                                if (item.isSimilar(hand)) {
                                                    if (removed + item.getAmount() > amount) {
                                                        item.setAmount((removed + item.getAmount()) - amount);
                                                        player.getInventory().setItem(slot, item);
                                                    } else {
                                                        player.getInventory().setItem(slot, null);
                                                    }
                                                    removed += item.getAmount();
                                                }
                                            }
                                            if (removed >= amount) {
                                                break;
                                            }
                                        }
                                        sendMessage(sender, ChatColor.GREEN + "Your auction has been queued as #" + (auctions.size() - 1));
                                    } else {
                                        sendMessage(sender, ChatColor.RED + "The queue is full!");
                                    }
                                } catch (NumberFormatException e) {
                                    showAucHelp(sender, args.length);
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("info")) {
                            if (ignoreBroadcast.contains(sender.getName())) {
                                sendMessage(sender, ChatColor.RED + "You must be listening to auctions to do that.");
                                return true;
                            }
                            Auction auction = auctions.getActiveAuction();
                            if (auction == null) {
                                sendMessage(sender, ChatColor.RED + "There is no active auction.");
                                return true;
                            }
                            auction.info(sender, false);
                        } else if (args[0].equalsIgnoreCase("showqueue")) {
                            if (ignoreBroadcast.contains(sender.getName())) {
                                sendMessage(sender, ChatColor.RED + "You must be listening to auctions to do that.");
                                return true;
                            }
                            List<Auction> queue = auctions.getAuctions();
                            if (queue.size() > 0) {
                                for (Auction auction : queue) {
                                    auction.info(sender, true);
                                }
                            } else {
                                sendMessage(sender, ChatColor.RED + "No queued auctions!");
                            }
                        } else if (args[0].equalsIgnoreCase("cancel")) {
                            if (ignoreBroadcast.contains(sender.getName())) {
                                sendMessage(sender, ChatColor.RED + "You must be listening to auctions to do that.");
                                return true;
                            }
                            Auction auction = auctions.getActiveAuction();
                            if (auction == null) {
                                sendMessage(sender, ChatColor.RED + "There is no active auction.");
                                return true;
                            }
                            if (auction.getSeller().equalsIgnoreCase(sender.getName()) || sender.hasPermission("dumbauction.admin")) {
                                auction.cancel(auctions);
                            } else {
                                sendMessage(sender, ChatColor.RED + "Not your auction!");
                            }
                        } else if (toggles.contains(args[0].toLowerCase())) {
                            if (ignoreBroadcast.contains(sender.getName())) ignoreBroadcast.remove(sender.getName());
                            else ignoreBroadcast.add(sender.getName());
                            getConfig().set("ignore-broadcast", ignoreBroadcast);
                            sendMessage(sender, ChatColor.YELLOW + "You are now " + (ignoreBroadcast.contains(sender.getName()) ? (ChatColor.RED + "IGNORING") : (ChatColor.GREEN + "NOT IGNORING")) + ChatColor.YELLOW + " auctions.");
                        } else if (args[0].equalsIgnoreCase("bid")) {
                            if (ignoreBroadcast.contains(sender.getName())) {
                                sendMessage(sender, ChatColor.RED + "You must be listening to auctions to do that.");
                                return true;
                            }
                            Auction auction = auctions.getActiveAuction();
                            if (auction == null) {
                                sendMessage(sender, ChatColor.RED + "There is no active auction.");
                                return true;
                            }
                            try {
                                double bid = args.length > 1 ? Double.parseDouble(args[1]) : auction.getHighBid() + auction.getBidIncrement();
                                if (DumbAuction.economy.has(sender.getName(), bid)) {
                                    if (!auction.bid(sender.getName(), bid)) {
                                        sendMessage(sender, ChatColor.RED + "Invalid bid! Please see the increment!");
                                    }
                                } else {
                                    sendMessage(sender, ChatColor.RED + "You cannot afford that!");
                                }
                            } catch (NumberFormatException e) {
                                sendMessage(sender, ChatColor.RED + "Invalid number!");
                            }
                        } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                            if (sender.hasPermission("dumbauction.admin")) {
                                saveConfig();
                                reloadConfig();
                            } else {
                                sendMessage(sender, ChatColor.RED + "You do not have permission to do that.");
                            }
                        } else {
                            sendMessage(sender, ChatColor.RED + "Incorrect syntax. Did you mean " + ChatColor.YELLOW + "/auc <start | info | showqueue | cancel | toggle | bid>" + ChatColor.RED + "?");
                        }
                    }
                } else {
                    sendMessage(sender, ChatColor.RED + "No");
                }
            }
        } else if (command.getName().equalsIgnoreCase("bid")) {
            return getServer().dispatchCommand(sender, "auc bid");
        } else {
            sendMessage(sender, ChatColor.RED + "Something broke.");
        }
        return true;
    }

    private void showAucHelp(CommandSender sender, int n) {
        String base = ChatColor.RED + "Incorrect syntax. Did you mean " + ChatColor.YELLOW + "/auc start <start price> <increment>";
        if (n >= 3) base += " <time>";
        if (n >= 4) base += " <amount>";
        base += ChatColor.RED + "?";
        sendMessage(sender, base);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage((ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix", ChatColor.GRAY + "[DumbAuction]")) + " " + ChatColor.WHITE + message).trim());
    }

    public void broadcast(String message) {
        for (Player player : getServer().getOnlinePlayers()) {
            if (!ignoreBroadcast.contains(player.getName())) {
                sendMessage(player, message);
            }
        }
        sendMessage(getServer().getConsoleSender(), message);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static String getName(Material name) {
        String def = name.name();
        if (p.getConfig().getString("aliases." + name.name()) != null) {
            return p.getConfig().getString("aliases." + name.name());
        }

        String[] parts = def.split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            builder.append(parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1).toLowerCase());
            builder.append(" ");
        }
        return builder.toString().trim();
    }

}
