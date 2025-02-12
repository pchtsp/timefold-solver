package ai.timefold.solver.core.impl.domain.variable.inverserelation;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.domain.variable.descriptor.VariableDescriptor;
import ai.timefold.solver.core.impl.domain.variable.listener.SourcedVariableListener;

import org.jspecify.annotations.NonNull;

/**
 * Alternative to {@link CollectionInverseVariableListener}.
 */
public class ExternalizedCollectionInverseVariableSupply<Solution_> implements
        SourcedVariableListener<Solution_>,
        VariableListener<Solution_, Object>,
        CollectionInverseVariableSupply {

    protected final VariableDescriptor<Solution_> sourceVariableDescriptor;

    protected Map<Object, Set<Object>> inverseEntitySetMap = null;

    public ExternalizedCollectionInverseVariableSupply(VariableDescriptor<Solution_> sourceVariableDescriptor) {
        this.sourceVariableDescriptor = sourceVariableDescriptor;
    }

    @Override
    public VariableDescriptor<Solution_> getSourceVariableDescriptor() {
        return sourceVariableDescriptor;
    }

    @Override
    public void resetWorkingSolution(@NonNull ScoreDirector<Solution_> scoreDirector) {
        inverseEntitySetMap = new IdentityHashMap<>();
        sourceVariableDescriptor.getEntityDescriptor().visitAllEntities(scoreDirector.getWorkingSolution(), this::insert);
    }

    @Override
    public void close() {
        inverseEntitySetMap = null;
    }

    @Override
    public void beforeEntityAdded(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Object entity) {
        insert(entity);
    }

    @Override
    public void beforeVariableChanged(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Object entity) {
        retract(entity);
    }

    @Override
    public void afterVariableChanged(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Object entity) {
        insert(entity);
    }

    @Override
    public void beforeEntityRemoved(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Object entity) {
        retract(entity);
    }

    @Override
    public void afterEntityRemoved(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Object entity) {
        // Do nothing
    }

    protected void insert(Object entity) {
        Object value = sourceVariableDescriptor.getValue(entity);
        if (value == null) {
            return;
        }
        Set<Object> inverseEntitySet = inverseEntitySetMap.computeIfAbsent(value,
                k -> Collections.newSetFromMap(new IdentityHashMap<>()));
        boolean addSucceeded = inverseEntitySet.add(entity);
        if (!addSucceeded) {
            throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                    + " because the entity (" + entity
                    + ") for sourceVariable (" + sourceVariableDescriptor.getVariableName()
                    + ") cannot be inserted: it was already inserted.");
        }
    }

    protected void retract(Object entity) {
        Object value = sourceVariableDescriptor.getValue(entity);
        if (value == null) {
            return;
        }
        Set<Object> inverseEntitySet = inverseEntitySetMap.get(value);
        boolean removeSucceeded = inverseEntitySet.remove(entity);
        if (!removeSucceeded) {
            throw new IllegalStateException("The supply (" + this + ") is corrupted,"
                    + " because the entity (" + entity
                    + ") for sourceVariable (" + sourceVariableDescriptor.getVariableName()
                    + ") cannot be retracted: it was never inserted.");
        }
        if (inverseEntitySet.isEmpty()) {
            inverseEntitySetMap.put(value, null);
        }
    }

    @Override
    public Collection<?> getInverseCollection(Object value) {
        Set<Object> inverseEntitySet = inverseEntitySetMap.get(value);
        if (inverseEntitySet == null) {
            return Collections.emptySet();
        }
        return inverseEntitySet;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + sourceVariableDescriptor.getVariableName() + ")";
    }

}
