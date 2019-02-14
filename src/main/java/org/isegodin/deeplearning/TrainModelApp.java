package org.isegodin.deeplearning;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.CollectionInputSplit;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.VGG16;
import org.isegodin.deeplearning.data.ModelTrainInfo;
import org.isegodin.deeplearning.util.UrlUtil;
import org.isegodin.deeplearning.util.ZipUtil;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author isegodin
 */
@Slf4j
public class TrainModelApp {

    private static final String TRAIN_DATA_URL = "https://dl.dropboxusercontent.com/s/tqnp49apphpzb40/dataTraining.zip?dl=0";

    private static final int SEED = 12345;
    private static final int NUM_POSSIBLE_LABELS = 2;
    private static final int EPOCH = 5;
    private static final int BATCH_SIZE = 16;
    private static final int TRAIN_SIZE = 85;
    private static final int SAVING_INTERVAL = 100;

    private static final Random RANDOM = new Random(SEED);
    private static final ParentPathLabelGenerator LABEL_GENERATOR_MAKER = new ParentPathLabelGenerator();
    private static final BalancedPathFilter PATH_FILTER = new BalancedPathFilter(RANDOM, BaseImageLoader.ALLOWED_FORMATS, LABEL_GENERATOR_MAKER);

    @SneakyThrows
    public static void main(String[] args) {
        String WORK_FOLDER = args[0];
        log.info("Work folder: {}", WORK_FOLDER);
        if (WORK_FOLDER == null || WORK_FOLDER.trim().isEmpty()) {
            throw new RuntimeException("Specify work folder");
        }

        File trainDataZipFile = UrlUtil.loadToFile(new URL(TRAIN_DATA_URL), Paths.get(WORK_FOLDER, "data.zip"));
        File trainDataFolder = Paths.get(WORK_FOLDER, "trainData").toFile();
        if (!trainDataFolder.exists()) {
            ZipUtil.unzip(trainDataZipFile, trainDataFolder);
        }

        File modelsFolder = Paths.get(WORK_FOLDER, "models").toFile();
        modelsFolder.mkdirs();

        ModelTrainInfo startModelTrainInfo = Optional.ofNullable(getLatestModel(modelsFolder.listFiles()))
                .orElseGet(() -> ModelTrainInfo.builder().epoch(0).iteration(-1).build());

        ComputationGraph vgg16Transfer = Optional.ofNullable(startModelTrainInfo.getFile())
                .map(TrainModelApp::loadModel)
                .orElseGet(TrainModelApp::prepareModel);

        vgg16Transfer.setListeners(new ScoreIterationListener(5));

        log.info("Model summary " + startModelTrainInfo + System.lineSeparator() + vgg16Transfer.summary());

        FileSplit train = new FileSplit(new File(trainDataFolder, "train_both"), NativeImageLoader.ALLOWED_FORMATS, RANDOM);
        FileSplit test = new FileSplit(new File(trainDataFolder, "test_both"), NativeImageLoader.ALLOWED_FORMATS, RANDOM);

        InputSplit[] samples = train.sample(PATH_FILTER, TRAIN_SIZE, 100 - TRAIN_SIZE);
        InputSplit trainSample = samples[0];

        DataSetIterator trainIterator = getDataSetIterator(trainSample);
        DataSetIterator devIterator = getDataSetIterator(samples[1]);

        DataSetIterator testIterator = getDataSetIterator(test.sample(PATH_FILTER, 1, 0)[0]);

        boolean firstIteration = true;

        while (vgg16Transfer.getEpochCount() < EPOCH) {
            DataSetIterator dataSetIterator = trainIterator;

            if (firstIteration && vgg16Transfer.getIterationCount() > 0) {
                dataSetIterator = getDataSetIterator(new CollectionInputSplit(Arrays.copyOfRange(
                        trainSample.locations(),
                        (vgg16Transfer.getIterationCount() + 1) * BATCH_SIZE,
                        (int) trainSample.length()
                )));
            }

            while (dataSetIterator.hasNext()) {
                DataSet trained = dataSetIterator.next();

                vgg16Transfer.fit(trained);

                if (vgg16Transfer.getIterationCount() % SAVING_INTERVAL == 0 && vgg16Transfer.getIterationCount() != 0) {
                    saveModel(vgg16Transfer,
                            modelsFolder,
                            ModelTrainInfo.builder()
                                    .epoch(vgg16Transfer.getEpochCount())
                                    .iteration(vgg16Transfer.getIterationCount())
                                    .build()
                                    .generateFileName());
                }
            }

            firstIteration = false;

            trainIterator.reset();
            evalOn(vgg16Transfer, testIterator);

            vgg16Transfer.incrementEpochCount();
            vgg16Transfer.getConfiguration().setIterationCount(0);

            saveModel(vgg16Transfer,
                    modelsFolder,
                    ModelTrainInfo.builder()
                            .epoch(vgg16Transfer.getEpochCount())
                            .iteration(vgg16Transfer.getIterationCount())
                            .build()
                            .generateFileName());
        }
    }

    private static ModelTrainInfo getLatestModel(File[] models) {
        return Stream.of(Optional.ofNullable(models).orElse(new File[0]))
                .map(ModelTrainInfo::fromFile)
                .max(ModelTrainInfo.COMPARATOR)
                .orElse(null);
    }

    @SneakyThrows
    private static ComputationGraph loadModel(File model) {
        return ModelSerializer.restoreComputationGraph(model);
    }

    @SneakyThrows
    private static ComputationGraph prepareModel() {
        final String FREEZE_UNTIL_LAYER = "fc2";

        ZooModel zooModel = VGG16.builder().build();
        ComputationGraph preTrainedNet = (ComputationGraph) zooModel.initPretrained(PretrainedType.IMAGENET);

        FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS.getIUpdaterWithDefaultConfig())
                .seed(SEED)
                .build();

        return new TransferLearning.GraphBuilder(preTrainedNet)
                .fineTuneConfiguration(fineTuneConf)
                .setFeatureExtractor(FREEZE_UNTIL_LAYER)
                .removeVertexKeepConnections("predictions")
                .addLayer("predictions",
                        new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nIn(4096).nOut(NUM_POSSIBLE_LABELS)
                                .weightInit(WeightInit.XAVIER)
                                .activation(Activation.SOFTMAX).build(), FREEZE_UNTIL_LAYER)
                .build();
    }

    private static void evalOn(ComputationGraph vgg16Transfer, DataSetIterator testIterator) {
        log.info("Evaluate model at epoch {} iteration {} ....", vgg16Transfer.getEpochCount(), vgg16Transfer.getIterationCount());
        Evaluation eval = vgg16Transfer.evaluate(testIterator);
        log.info(eval.stats());
        testIterator.reset();
    }

    @SneakyThrows
    private static void saveModel(ComputationGraph computationGraph, File modelsFolder, String modelFileName) {
        ModelSerializer.writeModel(computationGraph, new File(modelsFolder, modelFileName), false);
        log.info("Model saved {}", modelFileName);
    }

    private static DataSetIterator getDataSetIterator(InputSplit sample) throws IOException {
        ImageRecordReader imageRecordReader = new ImageRecordReader(224, 224, 3, LABEL_GENERATOR_MAKER);
        imageRecordReader.initialize(sample);

        DataSetIterator iterator = new RecordReaderDataSetIterator(imageRecordReader, BATCH_SIZE, 1, NUM_POSSIBLE_LABELS);
        iterator.setPreProcessor(new VGG16ImagePreProcessor());
        return iterator;
    }

}
