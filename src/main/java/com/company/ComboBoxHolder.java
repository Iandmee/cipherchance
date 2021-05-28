package com.company;

public class ComboBoxHolder {
    private String title;
    private Object data;

    public ComboBoxHolder(String name, Object value) {
        title = name;
        data = value;
    }

    @Override
    public String toString() {
        return title;
    }

    public Object getData() {
        return data;
    }
}
