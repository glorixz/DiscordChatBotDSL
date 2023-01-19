package ast;

import ast.evaluator.DSLBotVisitor;

import java.util.List;
import java.util.Objects;

public class CondChain extends FnStatement {

    private final List<IfStatement> ifStatements;
    private final FnBody elseStatement;

    public CondChain(List<IfStatement> ifStatements, FnBody elseStatement) {
        this.ifStatements = ifStatements;
        this.elseStatement = elseStatement;
    }

    public List<IfStatement> getIfs() {
        return ifStatements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CondChain that = (CondChain) o;
        return ifStatements.equals(that.ifStatements) && Objects.equals(elseStatement, that.elseStatement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifStatements, elseStatement);
    }

    public FnBody getElseStatement() {
        return elseStatement;
    }


    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
