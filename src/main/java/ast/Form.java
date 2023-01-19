package ast;

import java.util.Objects;
import ast.evaluator.DSLBotVisitor;

public class Form extends FnStatement {

    private TextVar responseVar;

    public Form(TextVar responseVar) {
        this.responseVar = responseVar;
    }

    public TextVar getResponseVar() {
        return responseVar;
    }

    public void setResponseVar(TextVar responseVar) {
        this.responseVar = responseVar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Form form = (Form) o;
        return Objects.equals(responseVar, form.responseVar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), responseVar);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
