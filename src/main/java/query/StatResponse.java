package query;

import java.util.List;

public class StatResponse implements Query {
    private List<Integer> blocks;

    public StatResponse(List<Integer> blocks) {
        this.blocks = blocks;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.STAT;
    }

    public List<Integer> getBlocks() {
        return blocks;
    }
}
