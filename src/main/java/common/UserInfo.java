package common;

import java.io.Serializable;
import java.util.Arrays;

public class UserInfo implements Serializable {
    private byte[] ip;
    private short port;

    public UserInfo(byte[] ip, short port) {
        this.ip = ip;
        this.port = port;
    }

    public byte[] getIp() {
        return ip;
    }

    public void setIp(byte[] ip) {
        this.ip = ip;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public boolean equals(Object other) {
        if(this == other)
            return true;
        if(!(other instanceof UserInfo))
            return false;
        UserInfo that = (UserInfo)other;
        return (this.ip == null ? that.ip == null : Arrays.equals(this.ip, that.ip)) &&
                (this.port == that.port);
    }
}
