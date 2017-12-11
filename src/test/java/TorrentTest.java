import client.ClientImpl;
import common.Tuple;
import common.UserInfo;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import tracker.TrackerImpl;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class TorrentTest {
    private static final String host = "127.0.0.1";
    private static final short port = 8081;
    private static final TrackerImpl tracker = new TrackerImpl(host, port);
    private static ClientImpl client1;
    private static ClientImpl client2;
    private static ClientImpl client3;
    private static String user1_file;
    private static String user2_file;
    private static String user3_file;
    private static String base_dir;

    public TorrentTest() {
    }

    private static void generateFiles(File root_dir) throws IOException {
        File temp1File = File.createTempFile("user1_file", ".txt", root_dir);
        user1_file = temp1File.getAbsolutePath();
        Random rnd = new Random();
        byte[] data = new byte[35 * 1024 * 1024];
        rnd.nextBytes(data);
        FileUtils.writeByteArrayToFile(temp1File, data);
        File temp2File = File.createTempFile("user2_file", ".txt", root_dir);
        user2_file = temp2File.getAbsolutePath();
        rnd.nextBytes(data);
        FileUtils.writeByteArrayToFile(temp2File, data);
        File temp3File = File.createTempFile("user3_file", ".txt", root_dir);
        user3_file = temp3File.getAbsolutePath();
        rnd.nextBytes(data);
        FileUtils.writeByteArrayToFile(temp3File, data);
    }

    private static File createDir() throws IOException {
        File root_dir = Paths.get(System.getProperty("user.dir"), "tmp").toFile();
        if (root_dir.exists()) {
            FileUtils.deleteDirectory(root_dir);
        }
        assertTrue(root_dir.mkdir());
        base_dir = root_dir.getAbsolutePath();
        File user1_dirs = new File(root_dir.getAbsolutePath() + "/user1");
        assertTrue(user1_dirs.mkdir());
        File user2_dirs = new File(root_dir.getAbsolutePath() + "/user2");
        assertTrue(user2_dirs.mkdir());
        File user3_dirs = new File(root_dir.getAbsolutePath() + "/user3");
        assertTrue(user3_dirs.mkdir());
        return root_dir;
    }

    private static void initClients(File root_dir) {
        final byte[] ip = {127, 0, 0, 1};
        final short port = 1234;
        client1 = new ClientImpl(host, ip, port, root_dir.getAbsolutePath() + "/user1");
        client2 = new ClientImpl(host, ip, (short)(port + 1), root_dir.getAbsolutePath() + "/user2");
        client3 = new ClientImpl(host, ip, (short)(port + 2), root_dir.getAbsolutePath() + "/user3");
    }

    private void compareFiles() throws IOException {
        File base_file1 = new File(user1_file);
        File base_file2 = new File(user2_file);
        File base_file3 = new File(user3_file);

        assertTrue(FileUtils.contentEquals(base_file1, new File(base_dir + "/user2/" + base_file1.getName())));
        assertTrue(FileUtils.contentEquals(base_file3, new File(base_dir + "/user2/" + base_file3.getName())));

        assertTrue(FileUtils.contentEquals(base_file2, new File(base_dir + "/user1/" + base_file2.getName())));
        assertTrue(FileUtils.contentEquals(base_file3, new File(base_dir + "/user1/" + base_file3.getName())));

        assertTrue(FileUtils.contentEquals(base_file1, new File(base_dir + "/user3/" + base_file1.getName())));
        assertTrue(FileUtils.contentEquals(base_file2, new File(base_dir + "/user3/" + base_file2.getName())));
    }

    @Before
    public void start() throws IOException {
        File root_dir = createDir();
        generateFiles(root_dir);
    }

    @After
    public void stop() throws IOException {
        client1.clear();
        client2.clear();
        client3.clear();
        tracker.clear();

        File root_dir = Paths.get(System.getProperty("user.dir"), "tmp").toFile();
        if (root_dir.exists()) {
            FileUtils.deleteDirectory(root_dir);
        }
    }

    @AfterClass
    public static void stopAll() {
        tracker.stop();
        client1.stop();
        client2.stop();
        client3.stop();
    }

    @BeforeClass
    public static void startAll() throws IOException {
        File root_dir = createDir();
        generateFiles(root_dir);
        initClients(root_dir);
        tracker.start();
        client1.start();
        client2.start();
        client3.start();
    }
    private void download() throws IOException, ExecutionException, InterruptedException {
        int file1_id = client1.upload(user1_file);
        int file2_id = client2.upload(user2_file);
        int file3_id = client3.upload(user3_file);

        List<Future<?>> futureList = new ArrayList<>();
        List<Tuple<Integer, String, Long>> list = client1.list();
        assertEquals(3, list.size());
        for (Tuple<Integer, String, Long> info : list) {
            if (info.getFirst() != file1_id) {
                futureList.add(client1.getFile(info.getFirst(), info.getSecond(), info.getThird()));
            }
            if (info.getFirst() != file2_id) {
                futureList.add(client2.getFile(info.getFirst(), info.getSecond(), info.getThird()));
            }
            if (info.getFirst() != file3_id) {
                futureList.add(client3.getFile(info.getFirst(), info.getSecond(), info.getThird()));
            }
        }
        for (Future<?> future : futureList) {
            future.get();
        }
        compareFiles();
    }

    @Test
    public void DownloadFileTest() throws IOException, ExecutionException, InterruptedException {
        download();
    }

    @Test
    public void SiderTest() throws ExecutionException, InterruptedException {
        int file1_id = client1.upload(user1_file);
        int file2_id = client2.upload(user2_file);
        List<Tuple<Integer, String, Long>> list = client1.list();
        for (Tuple<Integer, String, Long> info : list) {
            if (info.getFirst() != file1_id) {
                client1.getFile(info.getFirst(), info.getSecond(), info.getThird()).get();
            }
        }
        assertEquals(2, client1.sources(file2_id).size());
    }

    @Test
    public void SaveLoadTest() throws InterruptedException, ExecutionException, IOException {
        download();
        List<Tuple<Integer, String, Long>> list = client1.list();
        assertEquals(3, list.size());
        tracker.save(base_dir);
        tracker.load(base_dir);
        list = client1.list();
        assertEquals(3, list.size());
        client1.save();
        client1.load();
    }

    @Test
    public void FiveMuniteCleanTest() throws InterruptedException {
        int file1_id = client1.upload(user1_file);
        byte[] ip = {127, 0, 0, 1};
        ClientImpl client4 = new ClientImpl(host, ip , (short) 6666, base_dir + "/user4");
        int file2_id = client4.upload(user2_file);
        List<Tuple<Integer, String, Long>> list = client1.list();
        assertEquals(2, list.size());
        client4.stop();
        List<UserInfo> sources = client1.sources(file2_id);
        assertEquals(1, sources.size());
        Thread.sleep(301000);
        sources = client1.sources(file2_id);
        assertEquals(0, sources.size());
        assertEquals(1, client1.sources(file1_id).size());
    }
}