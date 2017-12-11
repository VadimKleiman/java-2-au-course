package query;

public class UpdateResponce implements Query {
    private boolean status;

    public UpdateResponce(boolean status) {
        this.status = status;
    }

    @Override
    public TypeQuery getQueryType() {
        return TypeQuery.UPDATE;
    }

    public boolean isStatus() {
        return status;
    }
}
