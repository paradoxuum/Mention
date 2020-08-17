package me.condolence.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
            mainConfig = gson.fromJson(fileCreated ? gson.toJson(MainConfig.class.newInstance()) : IOUtils.toString(inputStream, StandardCharsets.UTF_8), MainConfig.class);
            if (fileCreated && (mainConfig != null)) { saveConfig(); }
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
