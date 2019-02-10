package org.isegodin.deeplearning.data;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author isegodin
 */
@Data
@Builder
public class ModelTrainInfo implements Comparable<ModelTrainInfo> {

    public static final Pattern NAME_PATTERN = Pattern.compile("epoch_(?<epoch>\\d+)_(index|iteration)_(?<iteration>\\d+)\\.zip");

    public static final Comparator<ModelTrainInfo> COMPARATOR = Comparator.comparingInt(ModelTrainInfo::getEpoch)
            .thenComparingInt(ModelTrainInfo::getIteration);

    private int epoch;
    private int iteration;
    private File file;

    public static ModelTrainInfo fromFile(File file) {
        Matcher matcher = NAME_PATTERN.matcher(file.getName());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unknown file name pattern " + file.getName());
        }

        return ModelTrainInfo.builder()
                .epoch(Integer.valueOf(matcher.group("epoch")))
                .iteration(Integer.valueOf(matcher.group("iteration")))
                .file(file)
                .build();
    }

    @Override
    public int compareTo(@NotNull ModelTrainInfo other) {
        return COMPARATOR.compare(this, other);
    }

    public String generateFileName() {
        return "epoch_" + epoch + "_iteration_" + iteration + ".zip";
    }
}
