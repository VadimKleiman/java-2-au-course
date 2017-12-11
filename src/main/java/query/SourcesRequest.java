package query;

public class SourcesRequest implements Query {
    private int id;

    public SourcesRequest(int id) {
        this.id = id;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.SOURCES;
    }

    public int getId() {
        return id;
    }
}
