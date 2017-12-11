package client;

import common.Tuple;
import common.UserInfo;

import java.util.List;
import java.util.concurrent.Future;

public interface Client {

    int upload(String path);

    List<Tuple<Integer, String, Long>> list();

    List<UserInfo> sources(int id);

    void start();

    void stop();

    List<Integer> getStat(byte []ip, short port, int id);

    Future<?> getFile(int id, String name, long size);

    void load();

    void save();

}
