package com.github.peacetrue.java_diff_utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ComputeDifference {

        //build simple lists of the lines of the two testfiles
        List<String> original = Arrays.asList("this is a test", "a test");
        List<String> revised = Arrays.asList("this is a testfile", "a test");

    @Test
    void basic() {

        //compute the patch: this is the diffutils part
        Patch<String> patch = DiffUtils.diff(original, revised);

        //simple output the computed patch to console
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            System.out.println(delta);
        }


    }


    @Test
    void patch() {
        // At first, parse the unified diff file and get the patch
        Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(patched);

// Then apply the computed patch to the given text
        List<String> result = DiffUtils.patch(original, patch);

//simple output to console
        System.out.println(result);
    }
}
