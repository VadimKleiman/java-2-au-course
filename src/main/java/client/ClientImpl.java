package client;

import common.*;
import exception.IOQueryException;
import exception.UndefinedQueryException;
import query.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClientImpl implements Client {
    private final int TPORT = 8081;
    private String trackerIP;
    private byte[] ip;
    private short port;
    private Thread updater;
    private Thread clientWorker;
    private ServerSocket socket;
    private String root_dir;
    private final Map<Integer, FileManager> files = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ExecutorService downloader = Executors.newCachedThreadPool();

    public ClientImpl(String trackerIP, byte[] ip, short port, String root_dir) {
        this.port = port;
        this.trackerIP = trackerIP;
        this.ip = ip;
        this.root_dir = root_dir;
        updater = new Thread(this::timeUpdate);
        updater.start();
    }

    @Override
    public int upload(String path) {
        try {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                Socket socket = new Socket(trackerIP, TPORT);
                IOObject io = new IOObject(socket);
                io.write(new UploadRequest(file.getName(), file.length()));
                Query query = io.read();
                socket.close();
                files.put(((UploadResponse) query).getId(), new FileManager(path));
                assert update();
                return ((UploadResponse) query).getId();
            }
        } catch (IOQueryException | IOException e) {
            e.printStackTrace();
        }
        assert false;
        return 0;
    }

    @Override
    public List<Tuple<Integer, String, Long>> list() {
        try {
            Socket socket = new Socket(trackerIP, TPORT);
            IOObject io = new IOObject(socket);
            io.write(new ListRequest());
            Query query = io.read();
            return ((ListResponse) query).getDocuments();
        } catch (IOException | IOQueryException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

    @Override
    public List<UserInfo> sources(int id) {
        try {
            Socket socket = new Socket(trackerIP, TPORT);
            IOObject io = new IOObject(socket);
            io.write(new SourcesRequest(id));
            Query query = io.read();
            return ((SourcesResponse) query).getSources();
        } catch (IOException | IOQueryException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

    @Override
    public void start() {
        String host =
                Byte.toString(ip[0]) + "." +
                Byte.toString(ip[1]) + "." +
                Byte.toString(ip[2]) + "." +
                Byte.toString(ip[3]);
        InetSocketAddress address = new InetSocketAddress(host, port);
        try {
            socket = new ServerSocket();
            socket.bind(address);
            clientWorker = new Thread(this::getClient);
            clientWorker.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (updater != null) {
            updater.interrupt();
        }
        if (downloader != null) {
            downloader.shutdown();
        }
        if (clientWorker != null) {
            clientWorker.interrupt();
        }
    }

    public void clear() {
        files.clear();
    }

    @Override
    public List<Integer> getStat(byte []ip, short port, int id) {
        String host =
                Byte.toString(ip[0]) + "." +
                Byte.toString(ip[1]) + "." +
                Byte.toString(ip[2]) + "." +
                Byte.toString(ip[3]);
        try {
            Socket socket = new Socket(host, port);
            IOObject io = new IOObject(socket);
            io.write(new StatRequest(id));
            Query query = io.read();
            socket.close();
            return ((StatResponse) query).getBlocks();
        } catch (IOException | IOQueryException e) {
            e.printStackTrace();
        }
        assert false;
        return null;
    }

    @Override
    public Future<?> getFile(int id, String name, long size) {
        return downloader.submit( () -> {
            files.put(id, new FileManager(this.root_dir + "/" + name, size));
            while (!files.get(id).isFull()) {
                List<UserInfo> sources = sources(id);
                for (UserInfo user : sources) {
                    byte[] ip = user.getIp();
                    short port = user.getPort();
                    try {
                        String host =
                                Byte.toString(ip[0]) + "." +
                                Byte.toString(ip[1]) + "." +
                                Byte.toString(ip[2]) + "." +
                                Byte.toString(ip[3]);
                        List<Integer> notAvailableBlocks = files.get(id).getNotAvailableBlocks();
                        for (Integer block : getStat(ip, port, id)) {
                            if (notAvailableBlocks.contains(block)) {
                                Socket socket = new Socket(host, port);
                                IOObject io = new IOObject(socket);
                                io.write(new GetRequest(id, block));
                                byte[] data = ((GetResponse) io.read()).getData();
                                files.get(id).write(block, data);
                                socket.close();
                                assert update();
                            }
                        }
                    } catch (IOException | IOQueryException e) {
                        e.printStackTrace();
                    }
                    if (files.get(id).isFull()) {
                        break;
                    }
                }
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load() {
        try {
            files.clear();
            ObjectInputStream out = new ObjectInputStream(new FileInputStream(root_dir + "/client_files"));
            Map<Integer, FileManager> tmp = (Map<Integer, FileManager>) out.readObject();
            files.putAll(tmp);
            for (Map.Entry<Integer, FileManager> entry : files.entrySet()) {
                entry.getValue().load();
            }
            out.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        update();
    }

    @Override
    public void save() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(root_dir + "/client_files"));
            out.writeObject(files);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean remove(int id) {
        files.remove(id);
        return true;
    }

    private void timeUpdate() {
        while (!Thread.interrupted()) {
            update();
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private boolean update() {
        try {
            Socket socket = new Socket(trackerIP, TPORT);
            IOObject io = new IOObject(socket);
            io.write(new UpdateRequest(ip, port, new ArrayList<>(files.keySet())));
            Query query = io.read();
            socket.close();
            return ((UpdateResponce) query).isStatus();
        } catch (IOException | IOQueryException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void getClient() {
        while (!Thread.interrupted()) {
            Socket clientSocket;
            try {
                clientSocket = socket.accept();
            } catch (IOException ignore) {
                break;
            }
            if (clientSocket == null) {
                continue;
            }
            executor.submit(() -> {
                try {
                    processing(clientSocket);
                } catch (UndefinedQueryException | IOQueryException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void processing(Socket clientSocket) throws UndefinedQueryException, IOQueryException {
        if (clientSocket.isClosed()) {
            return;
        }
        IOObject io = new IOObject(clientSocket);
        Query query = io.read();
        if (query == null) {
            return;
        }
        switch (query.getQueryType()) {
            case GET:
                get(io, (GetRequest) query);
                break;
            case STAT:
                stat(io, (StatRequest) query);
                break;
            default:
                throw new UndefinedQueryException("Undefined query");
        }
    }

    private void stat(IOObject io, StatRequest query) throws IOQueryException {
        int id = query.getId();
        synchronized (files) {
            if (files.containsKey(id)) {
                io.write(new StatResponse(files.get(id).getAvailableBlocks()));
            } else {
                io.write(new StatResponse(new ArrayList<>()));
            }
        }
    }

    private void get(IOObject io, GetRequest query) throws IOQueryException {
        int id = query.getId();
        int part = query.getPart();
        try {
            byte[] data = files.get(id).read(part);
            io.write(new GetResponse(data));

        } catch (Exception ignore) {
            io.write(new GetResponse(null));
        }
    }
}
