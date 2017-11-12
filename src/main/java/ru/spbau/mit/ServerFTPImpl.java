package ru.spbau.mit;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerFTPImpl implements ServerFTP {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int port;
    private ServerSocket serverSocket;
    private int EMPTY = 0;

    public ServerFTPImpl(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                while (true) {
                    Socket socket = serverSocket.accept();
                    executor.submit(() -> processing(socket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void get(String path, DataOutputStream out) throws IOException {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            out.writeLong(file.length());
            out.writeBytes(new String(Files.readAllBytes(Paths.get(path))));
        } else {
            out.writeLong(EMPTY);
        }
    }

    private void list(String path, DataOutputStream out) throws IOException {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                out.writeInt(EMPTY);
                return;
            }
            out.writeInt(files.length);
            for (File file : files) {
                out.writeUTF(file.getAbsolutePath());
                out.writeBoolean(file.isDirectory());
            }
        } else {
            out.writeInt(EMPTY);
        }
    }

    private void processing(Socket socket) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            int type;
            String path;
            while (true) {
                type = in.readInt();
                switch (type) {
                    case 1:
                        path = in.readUTF();
                        list(path, out);
                        break;
                    case 2:
                        path = in.readUTF();
                        get(path, out);
                        break;
                    default:
                        return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
