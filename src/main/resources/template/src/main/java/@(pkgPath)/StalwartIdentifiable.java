package @(pkg);

import org.jspecify.annotations.Nullable;
import rs.ltt.jmap.common.entity.Identifiable;

public interface StalwartIdentifiable extends Identifiable {
    // TODO: figure out how new entities without IDs are handled
    @Nullable
    String id();

    @Override
    default String getId() {
        return id();
    }
}
