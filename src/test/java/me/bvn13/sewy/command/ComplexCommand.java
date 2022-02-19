package me.bvn13.sewy.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ComplexCommand extends AbstractCommand {

    private List<SimpleData> datum;

    public ComplexCommand() {
        datum = new ArrayList<>();
    }

    public ComplexCommand(List<SimpleData> datum) {
        this.datum = datum;
    }

    public void add(SimpleData data) {
        datum.add(data);
    }

    public ComplexCommand setDatum(List<SimpleData> datum) {
        this.datum = datum;
        return this;
    }

    public List<SimpleData> getDatum() {
        return datum;
    }

    public static class SimpleData implements Serializable {
        private final String string;

        public SimpleData(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

}
