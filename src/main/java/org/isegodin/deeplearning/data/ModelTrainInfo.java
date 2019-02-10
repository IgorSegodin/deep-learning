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

    public static final Pattern NAME_PATTERN = Pattern.compile("epoch_(?<epoch>\\d+)_index_(?<index>\\d+)\\.zip");

    public static final Comparator<ModelTrainInfo> COMPARATOR = Comparator.comparingInt(ModelTrainInfo::getEpoch)
            .thenComparingInt(ModelTrainInfo::getIndex);

    private int epoch;
    private int index;
    private File file;

    public static ModelTrainInfo fromFile(File file) {
        Matcher matcher = NAME_PATTERN.matcher(file.getName());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unknown file name pattern " + file.getName());
        }

        return ModelTrainInfo.builder()
                .epoch(Integer.valueOf(matcher.group("epoch")))
                .index(Integer.valueOf(matcher.group("index")))
                .file(file)
                .build();
    }

    @Override
    public int compareTo(@NotNull ModelTrainInfo other) {
        return COMPARATOR.compare(this, other);
    }

    public String generateFileName() {
        return "epoch_" + epoch + "_index_" + index + ".zip";
    }
}
