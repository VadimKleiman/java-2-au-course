package query;

import java.io.Serializable;

public interface Query extends Serializable {
    TypeQuery getQueryType();
}
