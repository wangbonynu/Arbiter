package org.deeplearning4j.arbiter.json;

import org.deeplearning4j.arbiter.ComputationGraphSpace;
import org.deeplearning4j.arbiter.GraphConfiguration;
import org.deeplearning4j.arbiter.MultiLayerSpace;
import org.deeplearning4j.arbiter.layers.ConvolutionLayerSpace;
import org.deeplearning4j.arbiter.layers.DenseLayerSpace;
import org.deeplearning4j.arbiter.layers.OutputLayerSpace;
import org.deeplearning4j.arbiter.multilayernetwork.TestDL4JLocalExecution;
import org.deeplearning4j.arbiter.optimize.api.CandidateGenerator;
import org.deeplearning4j.arbiter.optimize.api.ParameterSpace;
import org.deeplearning4j.arbiter.optimize.api.data.DataProvider;
import org.deeplearning4j.arbiter.optimize.api.termination.MaxCandidatesCondition;
import org.deeplearning4j.arbiter.optimize.api.termination.MaxTimeCondition;
import org.deeplearning4j.arbiter.optimize.candidategenerator.RandomSearchGenerator;
import org.deeplearning4j.arbiter.optimize.config.OptimizationConfiguration;
import org.deeplearning4j.arbiter.optimize.parameter.continuous.ContinuousParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.discrete.DiscreteParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.integer.IntegerParameterSpace;
import org.deeplearning4j.arbiter.saver.local.graph.LocalComputationGraphSaver;
import org.deeplearning4j.arbiter.scoring.ScoreFunctions;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.junit.Test;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Alex on 14/02/2017.
 */
public class TestJson {

    @Test
    public void testMultiLayerSpaceJson(){

        MultiLayerSpace mls = new MultiLayerSpace.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(new ContinuousParameterSpace(0.0001, 0.2))
                .regularization(true)
                .l2(new ContinuousParameterSpace(0.0001, 0.05))
                .dropOut(new ContinuousParameterSpace(0.2, 0.7))
                .iterations(1)
                .addLayer(new DenseLayerSpace.Builder()
                        .nIn(1).nOut(new IntegerParameterSpace(5, 30))
                        .activation(new DiscreteParameterSpace<>("relu","softplus","leakyrelu"))
                        .build(), new IntegerParameterSpace(1, 2), true) //1-2 identical layers
                .addLayer(new DenseLayerSpace.Builder().nIn(4).nOut(new IntegerParameterSpace(2, 10))
                        .activation(new DiscreteParameterSpace<String>("relu", "tanh"))
                        .build(), new IntegerParameterSpace(0, 1), true)   //0 to 1 layers
                .addLayer(new OutputLayerSpace.Builder().nOut(10).activation("softmax")
                        .iLossFunction(LossFunctions.LossFunction.MCXENT.getILossFunction()).build())
                .setInputType(InputType.convolutional(28,28,1))
                .pretrain(false).backprop(true).build();

        String asJson = mls.toJson();
        String asYaml = mls.toYaml();

//        System.out.println(asJson);
//        System.out.println(asYaml);

        MultiLayerSpace fromJson = MultiLayerSpace.fromJson(asJson);
//        MultiLayerSpace fromYaml = MultiLayerSpace.fromYaml(asYaml);

        assertEquals(mls, fromJson);
//        assertEquals(mls, fromYaml);


    }

    @Test
    public void testComputationGraphSpaceJson(){

        ParameterSpace<Integer> p = new IntegerParameterSpace(10,100);
        ComputationGraphSpace cgs = new ComputationGraphSpace.Builder()
                .learningRate(new DiscreteParameterSpace<Double>(0.1, 0.5, 1.0))
                .seed(12345)
                .addInputs("in")
                .addLayer("0", new DenseLayerSpace.Builder().nIn(new IntegerParameterSpace(1,100)).nOut(p).build(), "in")
                .addLayer("1",new DenseLayerSpace.Builder().nIn(p).nOut(10).build(), "0")
                .addLayer("2", new OutputLayerSpace.Builder().iLossFunction(LossFunctions.LossFunction.MCXENT.getILossFunction()).nIn(10).nOut(5).build(), "1")
                .setOutputs("2")
                .backprop(true).pretrain(false)
                .build();

        String asJson = cgs.toJson();
        ComputationGraphSpace fromJson = ComputationGraphSpace.fromJson(asJson);

        assertEquals(cgs, fromJson);
    }
}
