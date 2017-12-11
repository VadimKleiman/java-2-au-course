package common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileManager implements Serializable {
    public static final long BLOCKSIZE = 10 * 1024 * 1024;
    private String name;
    private transient RandomAccessFile file;

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    private final class Block implements Serializable {
        private int id;
        private boolean status;

        public Block(int id, boolean status) {
            this.id = id;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public byte[] getData() {
            if (status) {
                try {
                    byte[] out = new byte[(int) BLOCKSIZE];
                    file.seek(id * BLOCKSIZE);
                    int count = file.read(out);
                    if (count < out.length) {
                        out = Arrays.copyOf(out, count);
                    }
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        public void setData(byte[] data) {
            try {
                file.seek(id *  BLOCKSIZE);
                file.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String path;
    private long size;
    private List<Block> blocks = new CopyOnWriteArrayList<>();

    public FileManager(String path) {
        this.path = path;
        File file = new File(path);
        name = file.getName();
        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                this.file = new RandomAccessFile(file, "rw");
                long countBlock = (file.length() + BLOCKSIZE - 1) / BLOCKSIZE;
                for (int i = 0; i < countBlock; i++) {
                    blocks.add(new Block(i, true));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public FileManager(String path, long size) {
        this.size = size;
        this.path = path;
        File file = new File(path);
        name = file.getName();
        try {
            this.file = new RandomAccessFile(file, "rw");
            this.file.setLength(size);
            long countBlock = (file.length() + BLOCKSIZE - 1) / BLOCKSIZE;
            for (int i = 0; i < countBlock; i++) {
                blocks.add(new Block(i, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAvailableBlocks() {
        List<Integer> out = new ArrayList<>();
        for (Block block : blocks) {
            if (block.isStatus()) {
                out.add(block.getId());
            }
        }
        return out;
    }

    public synchronized void write(int id, byte[] data) {
        blocks.get(id).setData(data);
        blocks.get(id).setStatus(true);
    }

    public synchronized byte[] read(int id) {
        return blocks.get(id).getData();
    }

    public List<Integer> getNotAvailableBlocks() {
        List<Integer> out = new ArrayList<>();
        for (Block block : blocks) {
            if (!block.isStatus()) {
                out.add(block.getId());
            }
        }
        return out;
    }

    public boolean isFull() {
        for (Block block : blocks) {
            if (!block.isStatus()) {
                return false;
            }
        }
        try {
            file.getFD().sync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void load() throws IOException {
        if (file != null) {
            file.close();
        }
        File file = new File(path);
        this.file = new RandomAccessFile(file, "rw");
    }
}
