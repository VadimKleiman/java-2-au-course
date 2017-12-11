package query;

public class UploadResponse implements Query {
    private int id;

    public UploadResponse(int id) {
        this.id = id;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.UPLOAD;
    }

    public int getId() {
        return id;
    }
}
