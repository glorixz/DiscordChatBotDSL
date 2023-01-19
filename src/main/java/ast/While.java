package ast;

import ast.evaluator.DSLBotVisitor;

import java.util.List;
import java.util.Objects;

public class While extends FnStatement {

    private final Comparee condition;
    private final FnBody body;

    public While(Comparee condition, FnBody body) {
        this.condition = condition;
        this.body = body;
    }

    public Comparee getCondition() {
        return condition;
    }


    public FnBody getBody() {
        return body;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        While aWhile = (While) o;
        return Objects.equals(condition, aWhile.condition) && Objects.equals(body, aWhile.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, body);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
