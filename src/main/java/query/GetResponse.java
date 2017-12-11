package query;

public class GetResponse implements Query {
    private byte[] data;

    public GetResponse(byte[] data) {
        this.data = data;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.GET;
    }

    public byte[] getData() {
        return data;
    }
}
