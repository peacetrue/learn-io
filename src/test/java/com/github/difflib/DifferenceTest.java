package com.github.difflib;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DifferenceTest {


    @SneakyThrows
    @Test
    void basic() {
        //build simple lists of the lines of the two testfiles
        List<String> original = Arrays.asList("deleted line", "this is a test", "a test");
        List<String> revised = Arrays.asList("this is a testfile", "a test", "new line");

        //compute the patch: this is the diffutils part
        Patch<String> patch = DiffUtils.diff(original, revised);

        //simple output the computed patch to console
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            System.out.println(delta);
        }

        // At first, parse the unified diff file and get the patch
        patch = UnifiedDiffUtils.parseUnifiedDiff(patch.getDeltas().stream().map(item -> item.toString()).collect(Collectors.toList()));

        // Then apply the computed patch to the given text
        List<String> result = DiffUtils.patch(original, patch);

        //simple output to console
        System.out.println(result);


    }

}
