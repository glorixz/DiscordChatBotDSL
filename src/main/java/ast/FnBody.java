package ast;

import ast.evaluator.DSLBotVisitor;

import java.util.List;
import java.util.Objects;

public class FnBody implements Statement {
    private final List<FnStatement> statements;

    public FnBody(List<FnStatement> statementList) {
        this.statements = statementList;
    }

    public List<FnStatement> getStatements() {
        return statements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FnBody fnBody = (FnBody) o;
        return statements.equals(fnBody.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statements);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
