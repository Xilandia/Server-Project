import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private int port;
    private String root;
    private String defaultPage;
    private int maxThreads;

    public Config(String configFile) throws IOException {
        Properties properties = new Properties();
        FileInputStream inputStream = new FileInputStream(configFile);
        properties.load(inputStream);
        inputStream.close();

        this.port = Integer.parseInt(properties.getProperty("port"));
        this.root = properties.getProperty("root");
        this.defaultPage = properties.getProperty("defaultPage");
        this.maxThreads = Integer.parseInt(properties.getProperty("maxThreads"));
    }

    public int getPort() {
        return port;
    }

    public String getRoot() {
        return root;
    }

    public String getDefaultPage() {
        return defaultPage;
    }

    public int getMaxThreads() {
        return maxThreads;
    }
}
