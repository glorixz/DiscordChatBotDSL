package ast;

import ast.evaluator.DSLBotVisitor;

import java.util.List;
import java.util.Objects;

public class Comparator extends Comparee {
// lhs rhs operator always non-null
    private final Comparee lhs;
    private final Comparee rhs;
    private final Operator operator;

    public Comparator(Comparee lhs, Comparee rhs, Operator operator) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
    }


    public Comparee getLhs() {
        return lhs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comparator that = (Comparator) o;
        return Objects.equals(lhs, that.lhs) && Objects.equals(rhs, that.rhs) && operator == that.operator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, rhs, operator);
    }

    public Comparee getRhs() {
        return rhs;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
