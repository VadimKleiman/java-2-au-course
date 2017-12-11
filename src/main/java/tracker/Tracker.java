package tracker;

import java.io.Serializable;

public interface Tracker extends Serializable {

    void start();

    void stop();

    void save(String path);

    void load(String path);
}
