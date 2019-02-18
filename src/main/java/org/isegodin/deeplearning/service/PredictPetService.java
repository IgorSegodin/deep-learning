package org.isegodin.deeplearning.service;

import lombok.SneakyThrows;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.isegodin.deeplearning.data.dict.PetType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author isegodin
 */
@Component
public class PredictPetService {

    private static final Double THRESHOLD = 0.75;

    private volatile ComputationGraph computationGraph;

    @SneakyThrows
    public void updateModel(InputStream model) {
        ComputationGraph newGraph = ModelSerializer.restoreComputationGraph(model);
        newGraph.init();

        this.computationGraph = newGraph;
    }

    @SneakyThrows
    public PetType predictFromStream(InputStream source) {
        try (InputStream is = source) {
            return predictPetType(is, computationGraph);
        }
    }

    @SneakyThrows
    public PetType predictFromUrl(URL fileUrl) {
        return predictPetType(fileUrl.openStream(), computationGraph);
    }

    @SneakyThrows
    private PetType predictPetType(InputStream imageSource, ComputationGraph computationGraph) {
        imageSource = new BufferedInputStream(imageSource);
        imageSource.mark(50);

        String contentType = URLConnection.guessContentTypeFromStream(imageSource);
        // TODO validate BaseImageLoader.ALLOWED_FORMATS

        NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
        INDArray image = loader.asMatrix(imageSource);
        DataNormalization scaler = new VGG16ImagePreProcessor();
        scaler.transform(image);

        INDArray output = computationGraph.outputSingle(false, image);

        double catChance = output.getDouble(0);
        double dogChance = output.getDouble(1);

        if (catChance > THRESHOLD) {
            return PetType.CAT;
        } else if (dogChance > THRESHOLD) {
            return PetType.DOG;
        } else {
            return PetType.NOT_KNOWN;
        }
    }

}
