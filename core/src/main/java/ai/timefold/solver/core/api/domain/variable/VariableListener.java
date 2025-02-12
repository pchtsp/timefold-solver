package ai.timefold.solver.core.api.domain.variable;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

import org.jspecify.annotations.NonNull;

/**
 * A listener sourced on a basic {@link PlanningVariable}.
 * <p>
 * Changes shadow variables when a source basic planning variable changes.
 * The source variable can be either a genuine or a shadow variable.
 * <p>
 * Important: it must only change the shadow variable(s) for which it's configured!
 * It should never change a genuine variable or a problem fact.
 * It can change its shadow variable(s) on multiple entity instances
 * (for example: an arrivalTime change affects all trailing entities too).
 * <p>
 * It is recommended to keep implementations stateless.
 * If state must be implemented, implementations may need to override the default methods
 * ({@link #resetWorkingSolution(ScoreDirector)}, {@link #close()}).
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Entity_> {@link PlanningEntity} on which the source variable is declared
 */
public interface VariableListener<Solution_, Entity_> extends AbstractVariableListener<Solution_, Entity_> {

    /**
     * When set to {@code true}, this has a performance loss.
     * When set to {@code false}, it's easier to make the listener implementation correct and fast.
     *
     * @return true to guarantee that each of the before/after methods is only called once per entity instance
     *         per operation type (add, change or remove).
     */
    default boolean requiresUniqueEntityEvents() {
        return false;
    }

    void beforeVariableChanged(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Entity_ entity);

    void afterVariableChanged(@NonNull ScoreDirector<Solution_> scoreDirector, @NonNull Entity_ entity);
}
