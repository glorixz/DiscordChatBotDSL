package ast;

import ast.evaluator.DSLBotVisitor;

public interface Node {
    public <T,E,U> U accept(DSLBotVisitor<T,E,U> v, T t, E e);
}
