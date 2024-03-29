/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.turt2live.dumbcoin;

import com.turt2live.commonsense.DumbPlugin;
import com.turt2live.commonsense.data.MySQL;
import com.turt2live.commonsense.data.NoDriverException;
import com.turt2live.dumbcoin.balance.BalanceManager;
import com.turt2live.dumbcoin.balance.MySQLBalanceManager;
import com.turt2live.dumbcoin.balance.YamlBalanceManager;
import com.turt2live.dumbcoin.vault.Economy_DumbCoin;
import com.turt2live.dumbcoin.vault.VaultImport;
import com.turt2live.hurtle.input.TimeMatcher;
import com.turt2live.hurtle.uuid.UUIDUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DumbCoin extends DumbPlugin {

    public static DumbCoin p;

    private BalanceManager manager;
    private TopBalanceManager topBalances;
    private VaultImport importer;

    @Override
    public void onEnable() {
        p = this;
        saveDefaultConfig();
        initCommonSense(72122);

        reloadPlugin();

        try {
            if (OfflinePlayer.class.getMethod("getUniqueId") == null) {
                throw new NoSuchMethodException(); // Hacky way of ensuring this is validated :D
            }
        } catch (NoSuchMethodException e) {
            getLogger().severe("--=================================================--");
            getLogger().severe("         YOUR SERVER VERSION IS NOT SUPPORTED        ");
            getLogger().severe("  DumbCoin relies on a method in OfflinePlayer to    ");
            getLogger().severe("  be present in order to operate. This method could  ");
            getLogger().severe("  not be found and therefore DumbCoin will now       ");
            getLogger().severe("  disable itself to avoid further problems.          ");
            getLogger().severe("                                                     ");
            getLogger().severe("  The fix? Update your server.                       ");
            getLogger().severe("--=================================================--");

            // Nope.
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getOnlineMode() && !getConfig().getBoolean("this-is-a-proxy-server-and-I-understand-the-risks-of-offline-mode", false)) {
            getLogger().warning("--=================================================--");
            getLogger().warning("          YOUR SERVER IS IN OFFLINE MODE             ");
            getLogger().warning("  DumbCoin will continue to operate, but due to how  ");
            getLogger().warning("  UUIDs work, support cannot be provided for your    ");
            getLogger().warning("  server, and you may have some interesting bugs.    ");
            getLogger().warning("                                                     ");
            getLogger().warning("  Please enable online mode to get proper support    ");
            getLogger().warning("  as well as avoid weird bugs. Please see BukkitDev  ");
            getLogger().warning("  for more information.                              ");
            getLogger().warning("                                                     ");
            getLogger().warning("  If your server is behind a proxy, please see the   ");
            getLogger().warning("  configuration. Disabling this warning while not    ");
            getLogger().warning("  being behind a proxy is just rude.                 ");
            getLogger().warning("--=================================================--");
        }

        if (getConfig().getBoolean("storage.use-mysql", false)) {
            try {
                MySQL sql = new MySQL(getConfig().getString("storage.mysql.hostname", "localhost"),
                        getConfig().getInt("storage.mysql.port", 3306),
                        getConfig().getString("storage.mysql.username", "user"),
                        getConfig().getString("storage.mysql.password", "pass"),
                        getConfig().getString("storage.mysql.database", "DumbCoin"));
                if (sql.connect() == MySQL.ConnectionStatus.CONNECTED && sql.isConnected()) {
                    manager = new MySQLBalanceManager(this, sql, new Queries(this, "mysql-data.sql"));
                } else {
                    getLogger().severe("Could not connect to MySQL, using YAML");
                    manager = new YamlBalanceManager(this);
                }
            } catch (NoDriverException e) {
                getLogger().severe("You have enabled MySQL but do not have a driver! Using YAML instead.");
                manager = new YamlBalanceManager(this);
            }
        } else
            manager = new YamlBalanceManager(this);
        topBalances = new TopBalanceManager();

        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            importer = new VaultImport();
            if (getConfig().getBoolean("advanced.register-vault", true)) {
                ServicesManager manager = getServer().getServicesManager();
                Class<? extends Economy> clazz = Economy_DumbCoin.class;
                String name = "DumbCoin";
                try {
                    Economy econ = clazz.getConstructor(DumbCoin.class).newInstance(this);
                    manager.register(Economy.class, econ, vault, ServicePriority.Highest);
                    vault.getLogger().info(String.format("[Economy] %s found: %s", name, econ.isEnabled() ? "Loaded" : "Waiting"));
                } catch (Exception e) {
                    vault.getLogger().severe(String.format("[Economy] There was an error hooking %s - check to make sure you're using a compatible version!", name));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        p = null;
        manager.save();
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("money")) {
            if (args.length < 1) {
                if (sender.hasPermission("money.balance")) {
                    sendMessage(sender, "You have " + format(manager.getBalance(UUIDUtils.getUUID(sender))));
                } else sendMessage(sender, ChatColor.RED + "No permission.");
            } else {
                String argName = null;
                double argAmount = 0;
                if (args.length >= 3) {
                    argName = args[1];
                    Player online = getServer().getPlayer(argName);
                    if (online != null) {
                        argName = online.getName();
                    }
                    try {
                        argAmount = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                    }
                }
                if (args[0].equalsIgnoreCase("pay")) {
                    if (!sender.hasPermission("money.pay")) sendMessage(sender, ChatColor.RED + "No permission");
                    else {
                        if (argName == null)
                            sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/money pay <player> <amount>");
                        else {
                            if (argAmount > 0) {
                                if (manager.hasEnough(UUIDUtils.getUUID(sender), argAmount)) {
                                    manager.pay(UUIDUtils.getUUID(sender), UUIDUtils.getUUID(argName), argAmount);
                                    sendMessage(sender, ChatColor.GREEN + "You have paid " + argName + " " + format(argAmount));
                                    Player payee = getServer().getPlayerExact(argName);
                                    if (payee != null) {
                                        sendMessage(payee, ChatColor.GREEN + sender.getName() + " has paid you " + format(argAmount));
                                    }
                                } else sendMessage(sender, ChatColor.RED + "You do not have enough!");
                            } else
                                sendMessage(sender, ChatColor.RED + "You must provide a positive, non-zero, number!");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("give")) {
                    if (!sender.hasPermission("money.give")) sendMessage(sender, ChatColor.RED + "No permission");
                    else {
                        if (argName == null)
                            sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/money give <player> <amount>");
                        else {
                            if (argAmount > 0) {
                                manager.deposit(UUIDUtils.getUUID(argName), argAmount);
                                sendMessage(sender, ChatColor.GREEN + "You have added " + format(argAmount) + " to " + argName);
                                Player target = getServer().getPlayerExact(argName);
                                if (target != null)
                                    sendMessage(target, ChatColor.GREEN + sender.getName() + " has sent you " + format(argAmount));
                            } else
                                sendMessage(sender, ChatColor.RED + "You must provide a positive, non-zero, number!");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("take")) {
                    if (!sender.hasPermission("money.take")) sendMessage(sender, ChatColor.RED + "No permission");
                    else {
                        if (argName == null)
                            sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/money take <player> <amount>");
                        else {
                            if (argAmount > 0) {
                                manager.withdraw(UUIDUtils.getUUID(argName), argAmount);
                                sendMessage(sender, ChatColor.GREEN + "You have taken " + format(argAmount) + " from " + argName);
                                Player target = getServer().getPlayerExact(argName);
                                if (target != null)
                                    sendMessage(target, ChatColor.GREEN + sender.getName() + " has taken " + format(argAmount) + " from you!");
                            } else
                                sendMessage(sender, ChatColor.RED + "You must provide a positive, non-zero, number!");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("set")) {
                    if (sender.hasPermission("money.balance")) {
                        if (!sender.hasPermission("money.set")) sendMessage(sender, ChatColor.RED + "No permission");
                        else {
                            if (argName == null)
                                sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/money set <player> <amount>");
                            else {
                                if (argAmount > 0) {
                                    manager.set(UUIDUtils.getUUID(argName), argAmount);
                                    sendMessage(sender, ChatColor.GREEN + "You have set " + argName + "'s account to " + format(argAmount));
                                } else
                                    sendMessage(sender, ChatColor.RED + "You must provide a positive, non-zero, number!");
                            }
                        }
                    } else sendMessage(sender, ChatColor.RED + "No permission.");
                } else if (args[0].equalsIgnoreCase("top")) {
                    FutureBalances future = new FutureBalances() {
                        @Override
                        public void accept(List<TopBalanceManager.PlayerBalance> balances, int minRank, int maxRank) {
                            if (balances.size() <= 0) {
                                sendMessage(sender, ChatColor.RED + "No balances for the range #" + (minRank + 1) + " - #" + (maxRank + 1));
                                return;
                            }
                            sendMessage(sender, ChatColor.YELLOW + "Balances, ranges #" + (minRank + 1) + " - #" + (maxRank + 1));
                            for (TopBalanceManager.PlayerBalance balance : balances) {
                                sendMessage(sender, ChatColor.AQUA + "#" + (balance.getRank() + 1) + ChatColor.GREEN + " " + balance.getPlayerName() + " " + ChatColor.DARK_GREEN + format(balance.getBalance()));
                            }
                        }
                    };
                    int perPage = 10;
                    int page = 0;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                            if (page <= 0) throw new NumberFormatException(); // I'm lazy
                            page--;
                        } catch (NumberFormatException e) {
                            sendMessage(sender, ChatColor.RED + "Not a valid page!");
                            return true;
                        }
                    }
                    sendMessage(sender, ChatColor.GRAY + "Fetching balances... please wait.");
                    int min = page * perPage;
                    int max = min + perPage - 1; // Inclusive
                    topBalances.getBalances(min, max, future);
                } else if (args[0].equalsIgnoreCase("help")) {
                    sendMessage(sender, ChatColor.GREEN + "/money" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Shows your current balance");
                    sendMessage(sender, ChatColor.GREEN + "/money <playername>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Shows <playername>'s balance");
                    sendMessage(sender, ChatColor.GREEN + "/money pay <player> <amount>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Pays <player> <amount> money");
                    sendMessage(sender, ChatColor.GREEN + "/money give <player> <amount>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Gives <player> <amount> money");
                    sendMessage(sender, ChatColor.GREEN + "/money take <player> <amount>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Takes <amount> money from <player>");
                    sendMessage(sender, ChatColor.GREEN + "/money set <player> <amount>" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Sets <player>'s account to have <amount> money");
                    sendMessage(sender, ChatColor.GREEN + "/money top [page]" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Shows top players ranked by balance");
                    sendMessage(sender, ChatColor.GREEN + "/money uuid" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Converts all non-UUID records to be UUID records");
                    sendMessage(sender, ChatColor.GREEN + "/money convert yaml mysql" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Converts YAML -> MySQL");
                    sendMessage(sender, ChatColor.GREEN + "/money convert mysql yaml" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Converts MySQL -> YAML");
                    sendMessage(sender, ChatColor.GREEN + "/money reload" + ChatColor.GRAY + " - " + ChatColor.AQUA + "Reloads the configuration");
                } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                    if (sender.hasPermission("money.reload")) {
                        reloadPlugin();
                        sendMessage(sender, ChatColor.GREEN + "Reloaded!");
                    } else sendMessage(sender, ChatColor.RED + "No permission.");
                } else if (args[0].equalsIgnoreCase("import")) {
                    if (sender.hasPermission("money.import")) {
                        if (importer != null) {
                            if (args.length > 1) {
                                String pluginName = args[1];
                                if (importer.canImport(pluginName)) {
                                    sendMessage(sender, ChatColor.GREEN + "Import started! See console for information (and completion)");
                                    importer.doImport(pluginName);
                                } else
                                    sendMessage(sender, ChatColor.RED + "That plugin was not found or is not supported by Vault!");
                            } else {
                                sendMessage(sender, ChatColor.GREEN + "Import started! See console for information (and completion)");
                                importer.doImport();
                            }
                        } else sendMessage(sender, ChatColor.RED + "Please enable Vault.");
                    } else sendMessage(sender, ChatColor.RED + "No permission.");
                } else if (args[0].equalsIgnoreCase("uuid")) {
                    if (sender.hasPermission("money.uuid")) {
                        sendMessage(sender, "Converting non-UUID records using a merge strategy...");
                        getServer().getScheduler().runTask(this, new UUIDChangeover(sender));
                    } else sendMessage(sender, ChatColor.RED + "No permission.");
                } else if (args[0].equalsIgnoreCase("convert")) {
                    if (sender.hasPermission("money.convert")) {
                        if (args.length < 3) {
                            sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/money convert <yaml/mysql> <yaml/mysql>");
                        } else {
                            String from = args[1];
                            String to = args[2];

                            BalanceConverter.Format fto = BalanceConverter.Format.fromName(to);
                            BalanceConverter.Format ffrom = BalanceConverter.Format.fromName(from);
                            if (fto == null || ffrom == null || ffrom == fto) {
                                sendMessage(sender, ChatColor.RED + "Incorrect syntax. Try " + ChatColor.YELLOW + "/money convert <yaml/mysql> <yaml/mysql>");
                            } else {
                                sendMessage(sender, "Starting conversion");
                                getServer().getScheduler().runTask(this, new BalanceConverter(ffrom, fto, sender));
                            }
                        }
                    } else sendMessage(sender, ChatColor.RED + "No permission.");
                } else {
                    // Assume player lookup
                    if (sender.hasPermission("money.balance.others")) {
                        sendMessage(sender, args[0] + " has " + format(manager.getBalance(UUIDUtils.getUUID(args[0]))));
                    } else sendMessage(sender, ChatColor.RED + "No permission.");
                }
            }
        } else {
            sendMessage(sender, ChatColor.RED + "Something broke.");
        }
        return true;
    }

    private void reloadPlugin() {
        reloadConfig();

        long saveEvery = TimeMatcher.getMilliseconds(getConfig().getString("advanced.save-every", "60s")) / 1000;
        if (saveEvery > 0) {
            getServer().getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    getLogger().info("Running periodic save");
                    manager.save();
                }
            }, saveEvery * 20, saveEvery * 20);
        }
    }


    public TopBalanceManager getTopBalanceManager() {
        return topBalances;
    }

    public BalanceManager getBalanceManager() {
        return manager;
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage((ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix", ChatColor.GRAY + "[DumbCoin]")) + " " + ChatColor.WHITE + message).trim());
    }

    public int getDecimalPlaces() {
        int v = getConfig().getInt("advanced.decimal-places", 2);
        if (v < 0) return 0;
        return v;
    }

    public String format(double v) {
        String baseFormat = getConfig().getString("advanced.money-format", "${AMOUNT}");
        String rounded = round(v, getDecimalPlaces()) + "";
        if (getDecimalPlaces() > 0) {
            String[] split = rounded.split("\\.");
            if (split[split.length - 1].length() < getDecimalPlaces()) {
                while (split[split.length - 1].length() < getDecimalPlaces()) {
                    split[split.length - 1] += "0"; // Padding
                }
                StringBuilder builder = new StringBuilder();
                for (String s : split) {
                    builder.append(s).append(".");
                }
                rounded = builder.toString().trim();
                rounded = rounded.substring(0, rounded.length() - 1);
            }
        } else {
            rounded = rounded.split("\\.")[0];
        }
        baseFormat = baseFormat.replaceAll("\\{AMOUNT\\}", rounded);
        return baseFormat;
    }

    public double round(double v, int n) {
        return new BigDecimal(v).setScale(n, RoundingMode.HALF_EVEN).doubleValue();
    }
}
