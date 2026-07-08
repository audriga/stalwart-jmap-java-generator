package @(pkg);

import java.util.Map;
import org.jspecify.annotations.Nullable;
import com.audriga.jmap.common.entity.Identifiable;
import com.audriga.jmap.common.method.call.standard.SetMethodCall;

public abstract class SingletonSetMethodCall<T extends Identifiable> extends SetMethodCall<T> {
    public SingletonSetMethodCall(String accountId, @Nullable String ifInState, Map<String, Object> update) {
        super(accountId, ifInState, null, Map.of("singleton", update), null, null);
    }
}
