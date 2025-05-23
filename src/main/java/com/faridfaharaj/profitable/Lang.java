package com.faridfaharaj.profitable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Lang {

    private final JavaPlugin plugin;
    private FileConfiguration lang;
    private final String[] langCodes = {"en"};

    public Lang(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultLangFiles();
        loadLang(plugin.getConfig().getString("lang", langCodes[0]));
    }

    public void saveDefaultLangFiles() {
        for(String langCode : langCodes){
            plugin.saveResource("lang/" + langCode + ".yml", false);
        }
    }

    private void loadLang(String langCode) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + langCode + "' not found. Falling back to English.");
            langFile = new File(plugin.getDataFolder(), "lang/" + langCodes[0] + ".yml");
        }

        lang = YamlConfiguration.loadConfiguration(langFile);
    }

    public String get(String path) {
        return lang.getString(path, "__missing translation[" + path + "]!__");
    }

}
