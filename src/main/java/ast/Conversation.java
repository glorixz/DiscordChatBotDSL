package ast;

import java.util.List;
import java.util.Objects;
import ast.evaluator.DSLBotVisitor;

public class Conversation implements Statement {

    private final String name;
    private final FnBody fnBody;

    public Conversation(String name, FnBody fnBody) {
        this.name = name;
        this.fnBody = fnBody;
    }

    public String getName() {
        return name;
    }

    public FnBody getFnBody() {
        return fnBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(name, that.name) && Objects.equals(fnBody, that.fnBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fnBody);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
