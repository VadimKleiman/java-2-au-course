package ru.spbau.mit;

import java.util.ArrayList;

public interface ClientFTP {
    boolean connect();

    void disconnect();

    ArrayList<Tuple<String, Boolean>> executeList(String path);

    ArrayList<Byte> executeGet(String path);
}
