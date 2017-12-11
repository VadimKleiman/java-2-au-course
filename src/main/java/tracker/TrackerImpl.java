package tracker;

import common.IOObject;
import common.Pair;
import common.Tuple;
import common.UserInfo;
import exception.IOQueryException;
import exception.UndefinedQueryException;
import query.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrackerImpl implements Tracker {
    private ServerSocket socket;
    private Thread clientWorker;
    private Thread cleaner;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final List<Pair<String, Long>> files = new ArrayList<>();
    private final Map<Integer, List<Pair<UserInfo, Long>>> id2users = new HashMap<>();

    public TrackerImpl(String ip, int port) {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        try {
            socket = new ServerSocket();
            socket.bind(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        files.clear();
        id2users.clear();
    }
    @Override
    public void start() {
        clientWorker = new Thread(this::getClient);
        clientWorker.start();
        cleaner = new Thread(this::cleanData);
        cleaner.start();
    }

    @Override
    public void stop() {
        if (socket == null)
        {
            return;
        }
        try {
            cleaner.interrupt();
            clientWorker.interrupt();
            executor.shutdown();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(String path) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path + "/files"));
            out.writeObject(files);
            out.close();
            out = new ObjectOutputStream(new FileOutputStream(path + "/id2users"));
            out.writeObject(id2users);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(String path) {
        try {
            id2users.clear();
            files.clear();
            ObjectInputStream in = new ObjectInputStream((new FileInputStream(path + "/files")));
            List<Pair<String, Long>> f = (List<Pair<String, Long>>) in.readObject();
            this.files.addAll(f);
            in.close();
            in = new ObjectInputStream((new FileInputStream(path + "/id2users")));
            Map<Integer, List<Pair<UserInfo, Long>>> users = (Map<Integer, List<Pair<UserInfo, Long>>>) in.readObject();
            id2users.putAll(users);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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
            case LIST:
                list(io);
                break;
            case UPDATE:
                update(io, (UpdateRequest) query);
                break;
            case SOURCES:
                sources(io, (SourcesRequest) query);
                break;
            case UPLOAD:
                upload(io, (UploadRequest) query);
                break;
            default:
                throw new UndefinedQueryException("Undefined query");
        }
    }

    private void list(IOObject io) throws IOQueryException {
        List<Tuple<Integer, String, Long>> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            result.add(new Tuple<>(i, files.get(i).getKey(), files.get(i).getValue()));
        }
        io.write(new ListResponse(result));
    }

    private void update(IOObject io, UpdateRequest query) throws IOQueryException {
        UserInfo userInfo = new UserInfo(query.getIp(), query.getPort());
        List<Integer> idx = query.getId();
        for (Integer anIdx : idx) {
            boolean contains = false;
            List<Pair<UserInfo, Long>> users = id2users.get(anIdx);
            for (Pair<UserInfo, Long> user : users) {
                if (user.getKey().equals(userInfo)) {
                    user.setValue(System.currentTimeMillis());
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                users.add(new Pair<>(userInfo, System.currentTimeMillis()));
            }
        }
        io.write(new UpdateResponce(true));
    }

    private void sources(IOObject io, SourcesRequest query) throws IOQueryException {
        List<UserInfo> users = new ArrayList<>();
        for (Pair<UserInfo, Long> u : id2users.get(query.getId())) {
            users.add(u.getKey());
        }
        io.write(new SourcesResponse(users));
    }

    private void upload(IOObject io, UploadRequest query) throws IOQueryException {
        int id;
        synchronized (files) {
            id = files.size();
            files.add(new Pair<>(query.getName(), query.getSize()));
        }
        id2users.put(id, Collections.synchronizedList(new ArrayList<>()));
        io.write(new UploadResponse(id));
    }

    private void cleanData() {
        while (!Thread.interrupted()) {
            try {
                long time = Long.MAX_VALUE;
                for (Map.Entry<Integer, List<Pair<UserInfo, Long>>> entry : id2users.entrySet())
                {
                    Iterator<Pair<UserInfo, Long>> iterator = entry.getValue().iterator();
                    while (iterator.hasNext()) {
                        try {
                            Pair<UserInfo, Long> next = iterator.next();

                            long t = 300000 - (System.currentTimeMillis() - next.getValue());
                            if (t < 0) {
                                iterator.remove();
                            } else {
                                if (t < time) {
                                    time = t;
                                }
                            }
                        } catch (Exception ignore) {
                            break;
                        }
                    }
                }
                if (time == Long.MAX_VALUE) {
                    continue;
                }
                Thread.sleep(time);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
