package ast;

import java.util.Objects;

public abstract class BotOutput extends FnStatement {

    private final StringBuild output;

    public BotOutput(StringBuild output) {
        this.output = output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotOutput botOutput = (BotOutput) o;
        return Objects.equals(output, botOutput.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(output);
    }

    public StringBuild getOutput() {
        return output;
    }

}
