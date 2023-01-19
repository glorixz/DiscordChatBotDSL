package ast;

import java.util.Objects;
import ast.evaluator.DSLBotVisitor;

public class InitCounterVar extends FnStatement implements Statement {

    private final String counterName;
    private final int value;

    public InitCounterVar(String counterName, int value) {
        this.counterName = counterName;
        this.value = value;
    }

    public String getCounterName() {
        return counterName;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitCounterVar that = (InitCounterVar) o;
        return value == that.value && Objects.equals(counterName, that.counterName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterName, value);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
