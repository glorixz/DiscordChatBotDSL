package ast;

import java.util.Objects;
import ast.evaluator.DSLBotVisitor;

public class IncDec extends FnStatement {
    private final CounterVar counterName;
    private final boolean isInc; // true if INCREMENT, false if DECREMENT

    public IncDec(CounterVar counterName, boolean isInc) {
        this.counterName = counterName;
        this.isInc = isInc;
    }

    public CounterVar getCounterName() {
        return counterName;
    }

    public boolean isInc() {
        return isInc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncDec incDec = (IncDec) o;
        return Objects.equals(counterName, incDec.counterName) && (isInc == incDec.isInc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterName, isInc);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
