package ru.spbau.mit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FTPTest {
    private ServerFTPImpl server;

    @Before
    public void before() {
        server = new ServerFTPImpl(1234);
        server.start();
    }

    @After
    public void after() {
        server.stop();
    }

    @Test
    public void connect() throws Exception {
        ClientFTPImpl clientFTP = new ClientFTPImpl("127.0.0.1", 1234);
        Assert.assertTrue(clientFTP.connect());
    }

    @Test
    public void executeList() throws Exception {
        ClientFTPImpl clientFTP = new ClientFTPImpl("127.0.0.1", 1234);
        clientFTP.connect();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("file.txt").getFile());
        ArrayList<Byte> bytes = clientFTP.executeGet(file.getAbsolutePath());
        final int checkSize = 14;
        Assert.assertEquals(checkSize, bytes.size());
        byte[] check = new byte[checkSize];
        for (int i = 0; i < checkSize; i++) {
            check[i] = bytes.get(i);
        }
        Assert.assertArrayEquals("Hello World!!!".getBytes(), check);
        bytes = clientFTP.executeGet("wrong/path");
        Assert.assertEquals(0, bytes.size());
    }

    @Test
    public void executeGet() throws Exception {
        ClientFTPImpl clientFTP = new ClientFTPImpl("127.0.0.1", 1234);
        clientFTP.connect();
        ClassLoader classLoader = getClass().getClassLoader();
        File path = new File(classLoader.getResource("A").getPath());
        ArrayList<Tuple<String, Boolean>> tuples = clientFTP.executeList(path.getAbsolutePath());
        final int checkSize = 3;
        Assert.assertEquals(checkSize, tuples.size());
        int count = 0;
        for (Tuple t : tuples) {
            if (t.getFirst().toString().endsWith("DIR1"))
            {
                Assert.assertTrue((boolean)t.getSecond());
                count++;
            } else if (t.getFirst().toString().endsWith("DIR2"))
            {
                Assert.assertTrue((boolean)t.getSecond());
                count++;
            } else
            {
                Assert.assertFalse((boolean)t.getSecond());
                count++;
            }
        }
        Assert.assertEquals(checkSize, count);
    }

}