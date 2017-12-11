package common;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import static org.junit.Assert.*;

public class FileManagerTest {

    @Test
    public void FileTest() throws IOException {
        File serverDir = Paths.get(System.getProperty("user.dir"), "tmp").toFile();
        if (serverDir.exists()) {
            FileUtils.deleteDirectory(serverDir);
        }
        assertTrue(serverDir.mkdir());
        Random rnd = new Random();
        byte[] data = new byte[150 * 1024 * 1024];
        rnd.nextBytes(data);
        long countBlock = (data.length + FileManager.BLOCKSIZE - 1) / FileManager.BLOCKSIZE;
        List<byte[]>  blocks = new ArrayList<>();
        for (int i = 0; i < countBlock; i++) {
            blocks.add(Arrays.copyOfRange(data, i, (int) (i + FileManager.BLOCKSIZE)));
        }
        FileManager fm = new FileManager(serverDir.getAbsolutePath() + "/testFile.txt", data.length);
        assertFalse(fm.isFull());
        for (int i = 0; i < countBlock; i++) {
            fm.write(i, blocks.get(i));
        }
        assertTrue(fm.isFull());
        assertEquals(0, fm.getNotAvailableBlocks().size());
        assertEquals(countBlock, fm.getAvailableBlocks().size());
        for (int i = 0; i < countBlock; i++) {
            assertArrayEquals(blocks.get(i), fm.read(i));
        }
        FileUtils.deleteDirectory(serverDir);
    }
}