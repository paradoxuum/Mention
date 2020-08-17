package me.condolence.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.condolence.PlayerMentionAddon;
import me.condolence.util.Debug;
import me.condolence.config.settings.MainConfig;
import net.labymod.addon.AddonLoader;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigHandler {
    private final Gson gson;
    private File configFile;

    private MainConfig mainConfig;

    public ConfigHandler() {
        gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    public void loadConfig() {
        // Create/get config file "settings.json" in "Mention" directory (addons-(version)/Mention/settings.json)
        configFile = new File(AddonLoader.getConfigDirectory() + "/Mention/settings.json");
        if (!configFile.getParentFile().exists()) {
            boolean dirCreated = configFile.getParentFile().mkdir();
            if (!dirCreated) {
                Debug.log("Config directory could not be created!");
                return;
            }
        }

        // Create new config file if it doesn't exist
        boolean fileCreated = false;
        if (!configFile.exists()) {
            try {
                fileCreated = configFile.createNewFile();
                if (!fileCreated) {
                    Debug.log("Settings file could not be created!");
                    return;
                }
            } catch (IOException e) {
                Debug.log("Could not create settings file!");
                e.printStackTrace();
                return;
            }
        }

        // Instantiate new input stream using the config file
        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            Debug.log("Settings file does not exist!");
            e.printStackTrace();
            return;
        }

        // Get config class from json and store as a field or create a new instance of the MainConfig class if the file did not exist previously
        try {
            boolean updateConfig = fileCreated;

            if (fileCreated) {
                mainConfig = gson.fromJson(gson.toJson(MainConfig.class.newInstance()), MainConfig.class);
            } else {
                String jsonString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                JsonElement configJsonElement = gson.fromJson(jsonString, JsonElement.class);
                JsonObject configJsonObject = configJsonElement.getAsJsonObject();
                String configVersion = configJsonObject.has("version") ? configJsonObject.get("version").getAsString() : "-1";

                if (configVersion.equals(PlayerMentionAddon.getVersion())) {
                    mainConfig = gson.fromJson(jsonString, MainConfig.class);
                } else {
                    mainConfig = gson.fromJson(gson.toJson(MainConfig.class.newInstance()), MainConfig.class);
                    updateConfig = true;
                    Debug.log("Config version does not equal addon version! Updating (and resetting) config...");
                }
            }

            if (updateConfig && (mainConfig != null)) { saveConfig(); }
        } catch (Exception e) {
            Debug.log("Failed to load settings!");
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        // Use PrintWriter to write the json file
        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8), true);
            printWriter.print(gson.toJson(mainConfig));
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            Debug.log("Failed to save to settings file!");
            e.printStackTrace();
        }
    }

    public MainConfig getMainConfig() { return mainConfig; }

    public File getConfigFile() { return configFile; }
}
