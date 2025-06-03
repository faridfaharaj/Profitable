package com.faridfaharaj.profitable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Lang {

    private final JavaPlugin plugin;
    private FileConfiguration lang;
    private final String[] langCodes = {"en","es"};

    TagResolver resolver = TagResolver.resolver(

    );

    public Lang(JavaPlugin plugin) throws IOException {
        this.plugin = plugin;
        saveDefaultLangFiles();
        loadLang(plugin.getConfig().getString("language", langCodes[0]));
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

    public Component get(String path, Map.Entry<String, String>... placeHolders) {

        String text = lang.getString(path, "<red>_missing translation:[" + path + "]!_</red>");

        for(Map.Entry<String, String> placeHolder : placeHolders){
            text = text.replace(placeHolder.getKey(), placeHolder.getValue());
        }

        return MiniMessage.miniMessage().deserialize(text);
    }

    public String getString(String path, Map.Entry<String, String>... placeHolders) {

        String text = lang.getString(path, "<red>_missing translation:[" + path + "]!_</red>");

        for(Map.Entry<String, String> placeHolder : placeHolders){
            text = text.replace(placeHolder.getKey(), placeHolder.getValue());
        }

        return text;
    }

    public List<Component> langToLore(String path, Map.Entry<String, String>... placeHolders) {
        List<String> lines = lang.getStringList(path);
        List<Component> lore = new ArrayList<>(lines.size());

        MiniMessage mini = MiniMessage.miniMessage();

        for (String line : lines) {
            StringBuilder sb = new StringBuilder(line);
            for (Map.Entry<String, String> entry : placeHolders) {
                String key = entry.getKey();
                String value = entry.getValue();
                int index;
                while ((index = sb.indexOf(key)) != -1) {
                    sb.replace(index, index + key.length(), value);
                }
            }

            String processed = sb.toString();
            if (processed.contains("%&new_line&%")) {
                for (String part : processed.split("%&new_line&%")) {
                    lore.add(mini.deserialize(part));
                }
            } else {
                lore.add(mini.deserialize(processed));
            }
        }

        return lore;
    }

}
