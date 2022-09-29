package com.github.peacetrue.learn.thread;

import com.github.peacetrue.spring.beans.BeanUtils;
import com.github.peacetrue.util.MapUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author peace
 **/
@Slf4j
public class ThreadTest {

    @SneakyThrows
    public static void main(String[] args) {
        Thread thread = Thread.currentThread();
        infoAncestors(thread.getThreadGroup());
    }

    @Test
    void basic() {
        Thread thread = Thread.currentThread();
        log.info("thread: {}", MapUtils.prettify(BeanUtils.getPropertyValues(thread)));
        log.info("ThreadGroup: {}", MapUtils.prettify(BeanUtils.getPropertyValues(thread.getThreadGroup())));
        infoAncestors(thread.getThreadGroup());
    }

    private static void infoAncestors(ThreadGroup threadGroup) {
        List<ThreadGroup> ancestors = ancestors(threadGroup, ThreadTest::isRoot, ThreadGroup::getParent);
        Collections.reverse(ancestors);
        log.info("ancestors.size: {}", ancestors.size());
        for (ThreadGroup ancestor : ancestors) {
            log.info("ThreadGroup: {}", MapUtils.prettify(BeanUtils.getPropertyValues(ancestor)));
        }
    }

    private static boolean isRoot(ThreadGroup group) {
        return group == null || group.getParent() == null || group == group.getParent();
    }

    public static <T> T root(T node, Predicate<T> root, Function<T, T> parent) {
        return root.test(node) ? node : root(parent.apply(node), root, parent);
    }

    public static <T> List<T> ancestors(T node, Predicate<T> root, Function<T, T> parent) {
        LinkedList<T> nodes = new LinkedList<>();
        ancestors(nodes, node, root, parent);
        return nodes;
    }

    public static <T> void ancestors(List<T> nodes, T node, Predicate<T> root, Function<T, T> parent) {
        nodes.add(node);
        if (!root.test(node)) ancestors(nodes, parent.apply(node), root, parent);
    }
}
