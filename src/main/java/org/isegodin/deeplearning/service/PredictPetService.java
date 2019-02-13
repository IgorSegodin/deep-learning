package org.isegodin.deeplearning.service;

import lombok.SneakyThrows;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.isegodin.deeplearning.data.dict.PetType;
import org.isegodin.deeplearning.util.UrlUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
    public void updateModel(byte[] model) {
        ComputationGraph newGraph = ModelSerializer.restoreComputationGraph(new ByteArrayInputStream(model));
        newGraph.init();

        this.computationGraph = newGraph;
    }

    public PetType predictFromBytes(byte[] bytes) {
        return predictPetType(new ByteArrayInputStream(bytes), computationGraph);
    }

    public PetType predictFromUrl(URL fileUrl) {
        return predictPetType(new ByteArrayInputStream(UrlUtil.loadToBytes(fileUrl)), computationGraph);
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
