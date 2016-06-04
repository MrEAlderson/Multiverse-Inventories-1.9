package com.tux2mc.debugreport;

import com.tux2mc.debugreport.ReportMakerThread;
import com.tux2mc.debugreport.StringReplacers;
import com.vexsoftware.votifier.Votifier;
import com.vexsoftware.votifier.net.VoteReceiver;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DebugReport
extends JavaPlugin {
    private boolean supportsuuid;
    private boolean vaultneedsupdating;
    private Economy economy;
    protected boolean econcompatmode;
    private Permission permission;
    private boolean votifierinstalled;
    private static DebugReport instance;
    private long enabled = 0;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("debugreport.report")) {
            this.createReport(sender);
            return true;
        }
        sender.sendMessage((Object)ChatColor.RED + "I'm sorry, but you can't generate debug reports!");
        return true;
    }

    public void onDisable() {
    }

    public void onEnable() {
        instance = this;
        this.enabled = System.currentTimeMillis();
        String[] cbversionstring = this.getServer().getVersion().split(":");
        Pattern pmcversion = Pattern.compile("(\\d+)\\.(\\d+)\\.?(\\d*)");
        Matcher mcmatch = pmcversion.matcher(cbversionstring[1]);
        if (mcmatch.find()) {
            try {
                int majorversion = Integer.parseInt(mcmatch.group(1));
                int minorversion = Integer.parseInt(mcmatch.group(2));
                if (mcmatch.group(3) != null) {
                    mcmatch.group(3).equals("");
                }
                if (majorversion == 1) {
                    if (minorversion > 6) {
                        if (minorversion == 7) {
                            this.supportsuuid = true;
                        } else if (minorversion > 7) {
                            this.supportsuuid = true;
                        }
                    }
                } else if (majorversion > 1) {
                    this.supportsuuid = true;
                }
            }
            catch (Exception majorversion) {
                // empty catch block
            }
        }
        try {
            this.initPlugins();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        this.checkVotifier();
    }

    private void initPlugins() throws Throwable {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return;
        }
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (this.supportsUUID()) {
            boolean vaultupdated = false;
            if (vault != null) {
                String[] version = vault.getDescription().getVersion().split("\\.");
                int majorver = Integer.parseInt(version[0]);
                int minorver = Integer.parseInt(version[1]);
                if (majorver > 1) {
                    vaultupdated = true;
                } else if (minorver > 3) {
                    vaultupdated = true;
                }
            }
            if (!vaultupdated) {
                this.vaultneedsupdating = true;
                return;
            }
        }
        this.initPermissions();
        this.setupEconomy();
    }

    private void setupEconomy() {
        RegisteredServiceProvider economyProvider = this.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (economyProvider != null) {
            this.economy = (Economy)economyProvider.getProvider();
            this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, new Runnable(){

                @Override
                public void run() {
                    if (DebugReport.this.supportsUUID() && !DebugReport.this.vaultneedsupdating) {
                        try {
                            DebugReport.this.economy.hasAccount(Bukkit.getOfflinePlayer((String)"Tux2"));
                        }
                        catch (AbstractMethodError e) {
                            DebugReport.this.econcompatmode = true;
                        }
                    }
                }
            }, 400);
        }
    }

    private void initPermissions() throws Throwable {
        RegisteredServiceProvider provider = Bukkit.getServicesManager().getRegistration((Class)Permission.class);
        if (provider == null) {
            return;
        }
        this.permission = (Permission)provider.getProvider();
        if (this.permission == null) {
            return;
        }
    }

    public boolean supportsUUID() {
        return this.supportsuuid;
    }

    private void checkVotifier() {
        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            this.votifierinstalled = true;
        }
    }

    public static DebugReport getInstance() {
        return instance;
    }

    public void createReport(CommandSender sender) {
        this.createReport(sender, null, "", null, null);
    }

    public void createReport(CommandSender sender, List<String> customdata) {
        this.createReport(sender, customdata, "", null, null);
    }

    public void createReport(CommandSender sender, String pluginname, File loglocation) {
        this.createReport(sender, null, pluginname, loglocation, null);
    }

    public void createReport(CommandSender sender, String pluginname, File loglocation, List<StringReplacers> replacements) {
        this.createReport(sender, null, pluginname, loglocation, replacements);
    }

    public void createReport(CommandSender sender, List<String> customdata, String pluginname, File loglocation) {
        this.createReport(sender, customdata, pluginname, loglocation, null);
    }

    public void createReport(CommandSender sender, List<String> customdata, String pluginname, File loglocation, List<StringReplacers> replacements) {
        sender.sendMessage((Object)ChatColor.GREEN + "Please wait as we generate the report");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
        Date date = new Date();
        StringBuilder report = new StringBuilder();
        report.append("DebugReport copyrighted 2014 by Joshua Reetz\nPortions copyrighted by Enjin.com and used with permission.\n");
        report.append("Debug Report generated on " + dateFormat.format(date) + "\n");
        report.append("Debug Report version: " + this.getDescription().getVersion() + "\n");
        StringBuilder uptime = new StringBuilder();
        long currenttime = System.currentTimeMillis();
        long elapsedtime = currenttime - this.enabled;
        long secondstime = elapsedtime / 1000;
        long seconds = secondstime % 60;
        long minutestime = secondstime / 60;
        long minutes = minutestime % 60;
        long hourstime = minutestime / 60;
        long hours = hourstime % 24;
        long daystime = hourstime / 24;
        if (daystime > 0) {
            uptime.append(String.valueOf(daystime) + " days ");
        }
        if (daystime > 0 || hours > 0) {
            uptime.append(String.valueOf(hours) + " hours ");
        }
        if (uptime.length() > 0 || minutes > 0) {
            uptime.append(String.valueOf(minutes) + " minutes ");
        }
        if (uptime.length() > 0 || seconds > 0) {
            uptime.append(String.valueOf(seconds) + " seconds");
        }
        report.append("Server uptime: " + uptime.toString() + "\n");
        if (this.permission != null) {
            report.append("Vault permissions system reported: " + this.permission.getName() + "\n");
        }
        if (this.economy != null) {
            report.append("Vault economy system reported: " + this.economy.getName() + "\n");
        }
        if (this.econcompatmode) {
            report.append("WARNING! Economy plugin doesn't support UUID, needs update.\n");
        }
        if (this.votifierinstalled) {
            String votiferversion = Bukkit.getPluginManager().getPlugin("Votifier").getDescription().getVersion();
            report.append("Votifier version: " + votiferversion + "\n");
            Plugin vobject = Bukkit.getPluginManager().getPlugin("Votifier");
            if (vobject != null && vobject instanceof Votifier) {
                Votifier votifier = (Votifier)vobject;
                boolean votifiererrored = false;
                if (votifier.getVoteReceiver() == null) {
                    votifiererrored = true;
                }
                FileConfiguration voteconfig = votifier.getConfig();
                String port = voteconfig.getString("port", "");
                String host = voteconfig.getString("host", "");
                report.append("Votifier is enabled properly: " + !votifiererrored + "\n");
                report.append("Votifier is listening on: " + host + ":" + port + "\n");
            }
        }
        report.append("Bukkit version: " + this.getServer().getVersion() + "\n");
        report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
        report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");
        if (customdata != null && customdata.size() > 0) {
            report.append("\n");
            for (String data : customdata) {
                report.append(String.valueOf(data) + "\n");
            }
        }
        report.append("\nPlugins: \n");
        Plugin[] votifiererrored = Bukkit.getPluginManager().getPlugins();
        int votifier = votifiererrored.length;
        int vobject = 0;
        while (vobject < votifier) {
            Plugin p = votifiererrored[vobject];
            report.append(String.valueOf(p.getName()) + " version " + p.getDescription().getVersion() + "\n");
            ++vobject;
        }
        report.append("\nWorlds: \n");
        for (World world : this.getServer().getWorlds()) {
            report.append(String.valueOf(world.getName()) + "\n");
        }
        ReportMakerThread rmthread = new ReportMakerThread(this, report, sender, loglocation, pluginname, replacements);
        Thread dispatchThread = new Thread(rmthread);
        dispatchThread.start();
    }

}

