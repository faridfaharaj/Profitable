package com.faridfaharaj.profitable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public String get(String path, Map.Entry<String, String>... placeHolders) {

        String text = lang.getString(path, "<red>_missing translation:[" + path + "]!_</red>");

        for(Map.Entry<String, String> placeHolder : placeHolders){
            text = text.replace(placeHolder.getKey(), placeHolder.getValue());
        }

        return text;
    }

    public Component getComponent(String path, Map.Entry<String, String>... placeHolders) {

        return MiniMessage.miniMessage().deserialize(get(path, placeHolders));

    }

    public List<Component> langToLore(String path, Map.Entry<String, String>... placeHolders){
        List<String> lines = lang.getStringList(path);

        List<Component> lore = new ArrayList<>();
        for (String line : lines){

            String miniMessage = line;
            for(Map.Entry<String, String> placeHolder : placeHolders){
                miniMessage = miniMessage.replace(placeHolder.getKey(), placeHolder.getValue());
            }
            if(miniMessage.contains("%&new_line&%")){
                String[] newLines = miniMessage.split("%&new_line&%");
                for(String newLine : newLines){
                    lore.add(MiniMessage.miniMessage().deserialize(newLine));
                }
            }else {
                lore.add(MiniMessage.miniMessage().deserialize(miniMessage));
            }

        }

        return lore;
    }

}
