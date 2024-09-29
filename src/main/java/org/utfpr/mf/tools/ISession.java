package org.utfpr.mf.tools;

import java.io.PrintStream;

public interface ISession {

    String getClassName();

    default PrintStream getPrintStream() {
        return System.out;
    }

    default void BEGIN(String sessionName) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getClassName()).append("] ").append(sessionName);
        getPrintStream().println(sb);
    }

    default void END() {}

    default void BEGIN_SUB(String sessionName) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t[>]").append(sessionName);
        getPrintStream().println(sb);
    }

    default void INFO(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t[INFO] ").append(message);
        getPrintStream().println(sb);
    }

    default void ERROR(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t[ERROR] ").append(message);
        getPrintStream().println(sb);
    }

}
