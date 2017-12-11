package query;

public class StatRequest implements Query {
    private int id;

    public StatRequest(int id) {
        this.id = id;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.STAT;
    }

    public int getId() {
        return id;
    }
}
