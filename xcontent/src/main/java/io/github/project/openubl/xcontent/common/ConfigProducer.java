package io.github.project.openubl.xcontent.common;

public class ConfigProducer {

    private static volatile Config instance;

    private ConfigProducer() {
        instance = new Config();
    }

    public static Config getInstance() {
        if (instance == null) {
            synchronized (ConfigProducer.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

}
