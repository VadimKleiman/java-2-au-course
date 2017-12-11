package query;

public class GetRequest implements Query {
    private int id;
    private int part;

    public GetRequest(int id, int part) {
        this.id = id;
        this.part = part;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.GET;
    }

    public int getId() {
        return id;
    }

    public int getPart() {
        return part;
    }
}
