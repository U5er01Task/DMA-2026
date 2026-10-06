package pr2;

import pr2.practice.Block1Auth;
import pr2.practice.Block2Lists;
import pr2.practice.Block3Network;
import pr2.practice.Block4UiState;
import pr2.practice.Block5Hardware;
import pr2.topics.Topic1Basics;
import pr2.topics.Topic2ControlFlow;
import pr2.topics.Topic3Strings;
import pr2.topics.Topic4Methods;
import pr2.topics.Topic5Classes;
import pr2.topics.Topic6Inheritance;
import pr2.topics.Topic7Interfaces;
import pr2.topics.Topic8Collections;
import pr2.topics.Topic9Exceptions;

/**
 * Практическая работа №2. Запуск всех заданий по очереди.
 * Каждый класс можно запустить и отдельно — у всех есть свой main.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Java for Android is ready!");
        System.out.println("Java " + System.getProperty("java.version") + ", " + System.getProperty("os.name"));
        System.out.println();

        Topic1Basics.main(args);
        Topic2ControlFlow.main(args);
        Topic3Strings.main(args);
        Topic4Methods.main(args);
        Topic5Classes.main(args);
        Topic6Inheritance.main(args);
        Topic7Interfaces.main(args);
        Topic8Collections.main(args);
        Topic9Exceptions.main(args);

        System.out.println();
        Block1Auth.main(args);
        Block2Lists.main(args);
        Block3Network.main(args);
        Block4UiState.main(args);
        Block5Hardware.main(args);
    }
}
