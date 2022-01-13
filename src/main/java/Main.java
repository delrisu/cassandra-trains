import asg.cliche.ShellFactory;
import backend.BackendException;
import backend.BackendSession;
import com.google.common.io.Resources;
import menu.Menu;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class Main {

  private static final String PROPERTIES_FILENAME = "config.properties";
  private static final String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();

  public static void main(String[] args) throws IOException {
    String contactPoint;
    String keyspace;

    Properties properties = new Properties();
    properties.load(new FileInputStream(rootPath + PROPERTIES_FILENAME));

    contactPoint = properties.getProperty("contact_point");
    keyspace = properties.getProperty("keyspace");

    BackendSession backendSession = null;

    try {
      backendSession = new BackendSession(contactPoint, keyspace);
    } catch (BackendException e) {
      e.printStackTrace();
    }

    ShellFactory.createConsoleShell("train-master-69", "", new Menu(backendSession)).commandLoop();
  }

}
