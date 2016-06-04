/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Server
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.tux2mc.debugreport;

import com.tux2mc.debugreport.DebugReport;
import com.tux2mc.debugreport.ReverseFileReader;
import com.tux2mc.debugreport.StringReplacers;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class ReportMakerThread
implements Runnable {
    DebugReport plugin;
    StringBuilder builder;
    CommandSender sender;
    File pluginlog;
    String pluginname;
    List<StringReplacers> replacements;

    public ReportMakerThread(DebugReport plugin, StringBuilder builder, CommandSender sender, File pluginlog, String pluginname, List<StringReplacers> replacements) {
        this.plugin = plugin;
        this.builder = builder;
        this.sender = sender;
        this.pluginlog = pluginlog;
        this.pluginname = pluginname;
        this.replacements = replacements;
    }

    @Override
    public synchronized void run() {
        File reportloc;
        block31 : {
            this.builder.append("\nLast Severe error message: \n");
            File serverloglocation = this.plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
            reportloc = new File(this.plugin.getDataFolder(), "reports");
            if (!reportloc.exists()) {
                reportloc.mkdirs();
            }
            try {
                File logfile = new File(String.valueOf(serverloglocation.getAbsolutePath()) + File.separator + "logs" + File.separator + "latest.log");
                if (!logfile.exists()) {
                    logfile = new File(String.valueOf(serverloglocation.getAbsolutePath()) + File.separator + "server.log");
                }
                ReverseFileReader rfr = new ReverseFileReader(logfile.getAbsolutePath());
                LinkedList<String> errormessages = new LinkedList<String>();
                String line = "";
                boolean errorfound = false;
                while ((line = rfr.readLine()) != null && !errorfound) {
                    if (errormessages.size() >= 40) {
                        errormessages.removeFirst();
                    }
                    errormessages.add(line);
                    if (!line.contains("[SEVERE]") && !line.contains("ERROR]")) continue;
                    boolean severeended = false;
                    while ((line = rfr.readLine()) != null && !severeended) {
                        if (line.contains("[SEVERE]") || line.contains("ERROR]")) {
                            if (errormessages.size() >= 40) {
                                errormessages.removeFirst();
                            }
                            errormessages.add(line);
                            continue;
                        }
                        severeended = true;
                    }
                    int i = errormessages.size();
                    while (i > 0) {
                        this.builder.append(String.valueOf((String)errormessages.get(i - 1)) + "\n");
                        --i;
                    }
                    errorfound = true;
                }
                rfr.close();
            }
            catch (Exception logfile) {
                // empty catch block
            }
            if (this.pluginlog != null && this.pluginlog.exists()) {
                try {
                    ReverseFileReader rfrlog = new ReverseFileReader(this.pluginlog.getAbsolutePath());
                    this.builder.append("\nLast 60 lines of " + this.pluginname + "'s log: \n");
                    LinkedList<String> enjinlogstuff = new LinkedList<String>();
                    String line = "";
                    int i = 0;
                    while (i < 60 && (line = rfrlog.readLine()) != null) {
                        enjinlogstuff.add(line);
                        ++i;
                    }
                    i = enjinlogstuff.size();
                    while (i > 0) {
                        this.builder.append(String.valueOf((String)enjinlogstuff.get(i - 1)) + "\n");
                        --i;
                    }
                    rfrlog.close();
                }
                catch (Exception rfrlog) {
                    // empty catch block
                }
            }
            File bukkityml = new File(serverloglocation + File.separator + "bukkit.yml");
            YamlConfiguration ymlbukkit = new YamlConfiguration();
            if (bukkityml.exists()) {
                try {
                    ymlbukkit.load(bukkityml);
                    if (ymlbukkit.getBoolean("settings.plugin-profiling", false)) {
                        this.plugin.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "timings merged");
                        try {
                            this.wait(2000);
                        }
                        catch (InterruptedException line) {
                            // empty catch block
                        }
                        boolean foundtimings = false;
                        int i = 99;
                        while (i >= 0 && !foundtimings) {
                            File timingsfile = i == 0 ? new File(serverloglocation + File.separator + "timings" + File.separator + "timings.txt") : new File(serverloglocation + File.separator + "timings" + File.separator + "timings" + i + ".txt");
                            if (timingsfile.exists()) {
                                String strLine;
                                foundtimings = true;
                                this.builder.append("\nTimings file output:\n");
                                FileInputStream fstream = new FileInputStream(timingsfile);
                                DataInputStream in = new DataInputStream(fstream);
                                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                                while ((strLine = br.readLine()) != null) {
                                    this.builder.append(String.valueOf(strLine) + "\n");
                                }
                                in.close();
                            }
                            --i;
                        }
                        break block31;
                    }
                    this.builder.append("\nTimings file output not enabled!\n");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        String fullreport = this.builder.toString();
        if (this.replacements != null) {
            for (StringReplacers replacement : this.replacements) {
                fullreport = fullreport.replaceAll(replacement.getRegex(), replacement.getReplacement());
            }
        }
        System.out.println(fullreport);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        Date date = new Date();
        BufferedWriter outChannel = null;
        try {
            outChannel = new BufferedWriter(new FileWriter(String.valueOf(reportloc.getAbsolutePath()) + File.separator + "report_" + dateFormat.format(date) + ".txt"));
            outChannel.write(fullreport);
            outChannel.close();
            this.sender.sendMessage((Object)ChatColor.GOLD + "Debug report created in " + reportloc.getAbsolutePath() + File.separator + "report_" + dateFormat.format(date) + ".txt successfully!");
        }
        catch (IOException e) {
            if (outChannel != null) {
                try {
                    outChannel.close();
                }
                catch (Exception br) {
                    // empty catch block
                }
            }
            this.sender.sendMessage((Object)ChatColor.DARK_RED + "Unable to write debug report!");
            e.printStackTrace();
        }
    }
}

