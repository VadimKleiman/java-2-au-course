package query;

import common.Tuple;
import java.util.List;

public class ListResponse implements Query {

    private List<Tuple<Integer, String, Long>> documents;

    public ListResponse(List<Tuple<Integer, String, Long>> documents) {
        this.documents = documents;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.LIST;
    }

    public List<Tuple<Integer, String, Long>> getDocuments() {
        return documents;
    }
}
