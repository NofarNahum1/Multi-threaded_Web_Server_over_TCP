public class Main {

    public static void main(String[] args) {
        MultiThreadedServer multiThreadedServer = new MultiThreadedServer();
        multiThreadedServer.loadConfiguration();
        multiThreadedServer.StartServer();
    }
}

