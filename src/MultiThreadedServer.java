import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadedServer {
    private static final String CONFIG_FILE = "config.ini";
    private static int serverPort;
    private static String rootDirectory;
    private static String defaultPage;
    private static int maxThreads;
    volatile static boolean isShutdownRequested = false;
    private static final Logger logger = Logger.getLogger(Main.class.getName());


    public void StartServer() {
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server started on port " + serverPort);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                isShutdownRequested = true;
                threadPool.shutdown();
                System.out.println("Server is shutting down...");
            }));

            while (!isShutdownRequested) {
                try {
                    Socket clientSocket = serverSocket.accept(); // waiting for a client to connect then returns a new Socket.
                    threadPool.submit(new ClientHandler(clientSocket, rootDirectory)); // takes this ClientHandler instance and schedules it for execution in a threads from the thread pool.
                }
                catch (IOException e) {
                    logger.log(Level.SEVERE, "Exception caught when trying to listen on port " + serverPort + " or listening for a connection", e);
                }
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred while setting up the server socket", e);
        }
    }

    // Reads server configuration from the config.ini file.
    public void loadConfiguration() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            serverPort = Integer.parseInt(prop.getProperty("port", "8080"));
            rootDirectory = prop.getProperty("root", "~/www/lab/html/");
            // defaultPage = prop.getProperty("defaultPage", "index.html");
            defaultPage = prop.getProperty("defaultPage", "params_info.html");
            maxThreads = Integer.parseInt(prop.getProperty("maxThreads", "10"));
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading configuration. Make sure the config.ini file is present.", ex);
            // System.out.println("Loaded configuration from: " + new File("src/" + CONFIG_FILE).getAbsolutePath());
            System.exit(1);
        }
    }
}


