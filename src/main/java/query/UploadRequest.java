package query;

public class UploadRequest implements Query {
    private String name;
    private long size;

    public UploadRequest(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.UPLOAD;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
