package query;

import common.UserInfo;

import java.util.List;

public class SourcesResponse implements Query {

    private List<UserInfo> sources;

    public SourcesResponse(List<UserInfo> sources) {
        this.sources = sources;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.SOURCES;
    }

    public List<UserInfo> getSources() {
        return sources;
    }
}
