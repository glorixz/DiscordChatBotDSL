package ast;

import java.util.Objects;
import ast.evaluator.DSLBotVisitor;

public class FnCall extends FnStatement {

    private final String fnName;

    public FnCall(String fnName) {
        this.fnName = fnName;
    }

    public String getFnName() {
        return fnName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FnCall fnCall = (FnCall) o;
        return Objects.equals(fnName, fnCall.fnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fnName);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
