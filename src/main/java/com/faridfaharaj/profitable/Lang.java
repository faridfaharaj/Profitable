package com.faridfaharaj.profitable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Lang {

    private final JavaPlugin plugin;
    private FileConfiguration lang;
    private final String[] langCodes = {"en"};

    public Lang(JavaPlugin plugin) throws IOException {
        this.plugin = plugin;
        saveDefaultLangFiles();
        loadLang(plugin.getConfig().getString("lang", langCodes[0]));
    }

    public void saveDefaultLangFiles() {
        for(String langCode : langCodes){
            plugin.saveResource("lang/" + langCode + ".yml", false);
        }
    }

    private void loadLang(String langCode) throws IOException {
        InputStream defaultLangInputStream = plugin.getResource("lang/" + langCodes[0] + ".yml");
        if(defaultLangInputStream == null) throw new FileNotFoundException("Language file 'en.yml' not found in JAR");

        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");

        FileConfiguration defaultlang = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultLangInputStream, StandardCharsets.UTF_8)
        );

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + langCode + "' not found. Falling back to English.");
            lang = defaultlang;
            return;
        }

        lang = YamlConfiguration.loadConfiguration(langFile);

        Set<String> keys = defaultlang.getKeys(true);
        boolean needsSave = false;
        for(String key : keys){
            if (!lang.contains(key)) {
                lang.set(key, defaultlang.get(key));
                needsSave = true;
                plugin.getLogger().info("Added new key to "+ langCode + ".yml: " + key);
            }
        }
        if (needsSave){
            lang.save(langFile);
        }
    }

    public String get(String path) {
        return lang.getString(path, "_missing translation:[" + path + "]!_");
    }

}
