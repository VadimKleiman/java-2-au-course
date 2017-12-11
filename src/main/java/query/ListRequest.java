package query;

public class ListRequest implements Query {

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.LIST;
    }
}
