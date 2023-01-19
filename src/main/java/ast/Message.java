package ast;

import ast.evaluator.DSLBotVisitor;

public class Message extends BotOutput {

    public Message(StringBuild output) {
        super(output);
    }

    @Override
    public <T, E, U> U accept(DSLBotVisitor<T, E, U> v, T t, E e) {
        return v.visit(this, t, e);
    }
}
