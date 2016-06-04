package Tux2.TuxTwoLib;

import Tux2.TuxTwoLib.DownloadPluginThread;
import Tux2.TuxTwoLib.TuxTwoLibWarningsListener;
import Tux2.TuxTwoLib.TuxTwoListener;
import Tux2.TuxTwoLib.WarningsThread;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class TuxTwoLib extends JavaPlugin {

   String ttlbuild = "6";
   public boolean hasupdate = false;
   public String newversion = "";
   public boolean updatefailed = false;
   boolean checkforupdates = true;
   boolean autodownloadupdates = true;
   boolean autodownloadupdateonnewmcversion = true;
   public boolean updatesuccessful = false;
   String currentMCversion = "1.9";
   String currentNMS = "v1_9_R1";
   String versionName = null;
   private String versionLink = null;
   String mcversion;
   private static final String TITLE_VALUE = "name";
   private static final String LINK_VALUE = "downloadUrl";
   boolean incompatiblemcversion;
   WarningsThread warnings;


   public TuxTwoLib() {
      this.mcversion = this.currentMCversion;
      this.incompatiblemcversion = false;
      this.warnings = null;
   }

   public void onEnable() {
      FileConfiguration config = this.getConfig();
      File configfile = new File(this.getDataFolder().toString() + "/config.yml");
      if(!configfile.exists()) {
         config.set("AutoDownloadUpdates", Boolean.valueOf(true));
         config.set("CheckForUpdates", Boolean.valueOf(true));
         config.set("AutoUpdateOnMinecraftVersionChange", Boolean.valueOf(true));
         this.saveConfig();
      }

      this.autodownloadupdates = config.getBoolean("AutoDownloadUpdates", true);
      this.autodownloadupdateonnewmcversion = config.getBoolean("AutoUpdateOnMinecraftVersionChange", true);
      this.checkforupdates = config.getBoolean("CheckForUpdates", true);
      Pattern bukkitversion = Pattern.compile("(\\d+\\.\\d+\\.?\\d*)-R(\\d\\.\\d)");
      String ver = this.getServer().getBukkitVersion();
      Matcher bukkitmatch = bukkitversion.matcher(ver);
      if(bukkitmatch.find()) {
         this.mcversion = bukkitmatch.group(1);
         String versioncheck;
         if(!this.mcversion.equals(this.currentMCversion) && !this.currentNMS.equals(this.getNMSVersion())) {
            if(this.autodownloadupdateonnewmcversion) {
               this.getLogger().warning("Current version incompatible with this version of Craftbukkit! Checking for and downloading a compatible version.");
               boolean versioncheck1 = this.updatePlugin(this.mcversion, false);
               if(versioncheck1 && !this.updatefailed) {
                  this.getLogger().warning("New version downloaded successfully. Make sure to restart your server to restore full functionality!");
               } else {
                  this.incompatiblemcversion = true;
                  this.getLogger().severe("New version download was unsuccessful. Please download the correct version of the library from http://dev.bukkit.org/server-mods/tuxtwolib/");
               }
            } else if(this.checkforupdates) {
               versioncheck = this.updateAvailable(this.mcversion, true);
               if(!versioncheck.equals("0")) {
                  this.newversion = versioncheck;
                  this.getLogger().severe("Craftbukkit revision is incompatible with this build! Please download " + this.newversion + " version of the library from http://dev.bukkit.org/server-mods/tuxtwolib/");
               } else {
                  this.getLogger().severe("Craftbukkit revision is incompatible with this build! Please download the correct version of the library from http://dev.bukkit.org/server-mods/tuxtwolib/");
               }
            } else {
               this.getLogger().severe("Craftbukkit revision is incompatible with this build! Please download the correct version of the library from http://dev.bukkit.org/server-mods/tuxtwolib/");
            }

            this.incompatiblemcversion = true;
            this.getServer().getPluginManager().registerEvents(new TuxTwoLibWarningsListener(this), this);
         } else {
            this.getServer().getPluginManager().registerEvents(new TuxTwoListener(), this);
            if(this.checkforupdates) {
               versioncheck = this.updateAvailable(this.mcversion, true);
               if(!versioncheck.equals("0")) {
                  this.newversion = versioncheck;
                  if(this.autodownloadupdates) {
                     this.getLogger().warning("Update available! Downloading in the background.");
                     if(!this.updatePlugin(this.mcversion, true)) {
                        this.getLogger().info("Update failed! Please download " + this.newversion + " version of the library from http://dev.bukkit.org/server-mods/tuxtwolib/ manually.");
                     }
                  } else {
                     this.getLogger().info("A new version for your version of Craftbukkit is available! Please download " + this.newversion + " version of the library from http://dev.bukkit.org/server-mods/tuxtwolib/");
                  }
               }

               this.getServer().getPluginManager().registerEvents(new TuxTwoLibWarningsListener(this), this);
            }
         }
      } else {
         this.getLogger().warning("Unable to verify minecraft version! MC version reported: " + ver);
      }

   }

   public void onDisable() {}

   public String updateAvailable(String version, boolean returnversion) {
      boolean result = this.read();
      return result?(returnversion?this.versionName:"1"):"0";
   }

   public boolean updatePlugin(String version, boolean threaded) {
      if(this.updateAvailable(version, false).equals("0")) {
         return false;
      } else {
         DownloadPluginThread dpt = new DownloadPluginThread(this.getDataFolder().getParent(), this.versionLink, new File(this.getServer().getUpdateFolder() + File.separator + this.getFile()), this);
         if(threaded) {
            Thread downloaderthread = new Thread(dpt);
            downloaderthread.start();
         } else {
            dpt.run();
         }

         return true;
      }
   }

   private boolean read() {
      String currentminecraftversion = this.mcversion;
      if(this.currentNMS.equals(this.getNMSVersion())) {
         currentminecraftversion = this.currentMCversion;
      }

      try {
         URL e = new URL("https://api.curseforge.com/servermods/files?projectIds=48210");
         URLConnection conn = e.openConnection();
         conn.setConnectTimeout(5000);
         conn.addRequestProperty("User-Agent", "Updater (by Gravity)");
         conn.setDoOutput(true);
         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String response = reader.readLine();
         JSONArray array = (JSONArray)JSONValue.parse(response);
         if(array.size() == 0) {
            this.getLogger().warning("The updater could not find any files for the TuxTwoLib project!");
            return false;
         } else {
            boolean foundupdate = false;

            for(int i = array.size() - 1; i < -1 && !foundupdate; --i) {
               this.versionName = (String)((JSONObject)array.get(i)).get("name");
               this.versionLink = (String)((JSONObject)array.get(i)).get("downloadUrl");
               String[] versionsplit = this.versionName.split("-");
               if(versionsplit.length > 1 && versionsplit[0].equalsIgnoreCase("v" + currentminecraftversion) && currentminecraftversion.equalsIgnoreCase(this.currentMCversion)) {
                  String buildnumber = versionsplit[1].substring(1);

                  try {
                     int build = Integer.parseInt(buildnumber);
                     int currentbuild = Integer.parseInt(this.ttlbuild);
                     if(currentbuild < build) {
                        foundupdate = true;
                     }
                  } catch (NumberFormatException var13) {
                     ;
                  }
               }
            }

            return foundupdate;
         }
      } catch (IOException var14) {
         if(var14.getMessage().contains("HTTP response code: 403")) {
            this.getLogger().warning("dev.bukkit.org rejected the API key provided in plugins/Updater/config.yml");
            this.getLogger().warning("Please double-check your configuration to ensure it is correct.");
         } else {
            this.getLogger().warning("The updater could not contact dev.bukkit.org for updating.");
            this.getLogger().warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
         }

         var14.printStackTrace();
         return false;
      }
   }

   private String getNMSVersion() {
      String name = this.getServer().getClass().getPackage().getName();
      String mcVersion = name.substring(name.lastIndexOf(46) + 1);
      return mcVersion;
   }
}
