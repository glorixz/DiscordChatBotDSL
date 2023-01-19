package ast;

import ast.evaluator.DSLBotVisitor;

public class CounterVar extends Variable {
    public CounterVar(String varName) {
        super(varName);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
