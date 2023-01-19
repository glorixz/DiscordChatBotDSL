package ast;

import java.util.List;
import java.util.Objects;
import ast.evaluator.DSLBotVisitor;

public class TriggerPhrase implements Statement {

    private final StringBuild phrase;
    private final FnBody body;

    public TriggerPhrase(StringBuild phrase, FnBody body) {
        this.phrase = phrase;
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggerPhrase that = (TriggerPhrase) o;
        return Objects.equals(phrase, that.phrase) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phrase, body);
    }

    public StringBuild getPhrase() {
        return phrase;
    }


    public FnBody getBody() {
        return body;
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
