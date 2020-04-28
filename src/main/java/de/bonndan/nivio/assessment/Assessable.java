package de.bonndan.nivio.assessment;

import de.bonndan.nivio.model.Component;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static de.bonndan.nivio.assessment.StatusValue.SUMMARY_LABEL;

/**
 * Interface for components that can be assessed and can have assigned {@link StatusValue}s.
 *
 *
 */
public interface Assessable extends Component {

    /**
     * Returns the highest status as summary of all {@link StatusValue} and children summaries.
     *
     * @return status value, field contains the component identifier, message is the identifier of the highest status value
     */
    default StatusValue getOverallStatus() {

        final AtomicReference<StatusValue> summary = new AtomicReference<>();
        summary.set(new StatusValue(SUMMARY_LABEL, Status.UNKNOWN));

        List<StatusValue> statusValues = new ArrayList<>(getStatusValues());
        getChildren().forEach(o -> statusValues.add(o.getOverallStatus()));
        statusValues.forEach(value -> {
            if (value == null) {
                return;
            }

            if (value.getStatus().isHigherThan(summary.get().getStatus())) {
                summary.set(value);
            }
        });

        return new StatusValue(SUMMARY_LABEL + "." + getIdentifier(), summary.get().getStatus(), summary.get().getField());
    }

    /**
     * Returns all status value
     *
     * @return a distinct (by field) set
     */
    Set<StatusValue> getStatusValues();

    /**
     * Set/overwrite the status for the assessed field.
     *
     * @param statusValue the new status value
     */
    default void setStatusValue(@NonNull StatusValue statusValue) {

        if (statusValue == null) {
            throw new IllegalArgumentException("Status value is null");
        }

        getStatusValues().add(statusValue);
    }

    /**
     * Returns the components to be assessed before this (e.g. group items).
     *
     */
    default List<? extends Assessable> getChildren() {
        return new ArrayList<>();
    }
}
