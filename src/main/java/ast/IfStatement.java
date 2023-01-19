package ast;

import ast.evaluator.DSLBotVisitor;

import java.util.Objects;

public class IfStatement implements Node {

    private final Comparee condition;
    private final FnBody body;

    public IfStatement(Comparee condition, FnBody body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfStatement ifStatement = (IfStatement) o;
        return Objects.equals(condition, ifStatement.condition) && Objects.equals(body, ifStatement.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, body);
    }

    public Comparee getCondition() {
        return condition;
    }


    public FnBody getBody() {
        return body;
    }


    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
