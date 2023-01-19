package ast;

import ast.evaluator.DSLBotVisitor;

import java.util.Objects;

public abstract class Variable extends Value {

    private final String varName;

    public Variable(String varName) {
        this.varName = varName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(varName, variable.varName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varName);
    }

    public String getVarName() {
        return varName;
    }
}
