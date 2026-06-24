package @(pkg);

import org.jspecify.annotations.Nullable;
import rs.ltt.jmap.common.entity.Identifiable;
import rs.ltt.jmap.common.method.call.standard.GetMethodCall;

public abstract class SingletonGetMethodCall<T extends Identifiable> extends GetMethodCall<T> {
    public SingletonGetMethodCall(String accountId, String @Nullable [] properties) {
        super(accountId, new String[]{"singleton"}, properties, null);
    }
}
