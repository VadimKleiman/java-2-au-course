package ru.spbau.mit;

import java.util.ArrayList;

public interface ClientFTP extends AutoCloseable {
    boolean connect();

    void close() throws Exception;

    ArrayList<Tuple<String, Boolean>> executeList(String path);

    ArrayList<Byte> executeGet(String path);
}
