package query;

import java.util.List;

public class UpdateRequest implements Query {
    private byte[] ip;
    private short port;
    private  List<Integer> id;

    public UpdateRequest(byte[] ip, short port, List<Integer> id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.UPDATE;
    }

    public byte[] getIp() {
        return ip;
    }

    public short getPort() {
        return port;
    }

    public List<Integer> getId() {
        return id;
    }
}
