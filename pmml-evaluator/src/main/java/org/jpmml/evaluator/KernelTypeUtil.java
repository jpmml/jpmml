/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class KernelTypeUtil {

	private KernelTypeUtil(){
	}

	static
	public double evaluate(KernelType kernelType, double[] input, double[] vector){

		if(kernelType instanceof LinearKernelType){
			return evaluateLinearKernel((LinearKernelType)kernelType, input, vector);
		} else

		if(kernelType instanceof PolynomialKernelType){
			return evaluatePolynomialKernel((PolynomialKernelType)kernelType, input, vector);
		} else

		if(kernelType instanceof RadialBasisKernelType){
			return evaluateRadialBasisKernel((RadialBasisKernelType)kernelType, input, vector);
		} else

		if(kernelType instanceof SigmoidKernelType){
			return evaluateSigmoidKernel((SigmoidKernelType)kernelType, input, vector);
		}

		throw new UnsupportedFeatureException(kernelType);
	}

	static
	public double evaluateLinearKernel(LinearKernelType linearKernelType, double[] input, double[] vector){
		return dotProduct(input, vector);
	}

	static
	public double evaluatePolynomialKernel(PolynomialKernelType polynomialKernelType, double[] input, double[] vector){
		return Math.pow(polynomialKernelType.getGamma() * dotProduct(input, vector) + polynomialKernelType.getCoef0(), polynomialKernelType.getDegree());
	}

	static
	public double evaluateRadialBasisKernel(RadialBasisKernelType radialBasisKernelType, double[] input, double[] vector){
		return Math.exp(-radialBasisKernelType.getGamma() * squaredDistance(input, vector));
	}

	static
	public double evaluateSigmoidKernel(SigmoidKernelType sigmoidKernelType, double[] input, double[] vector){
		return Math.tanh(sigmoidKernelType.getGamma() * dotProduct(input, vector) + sigmoidKernelType.getCoef0());
	}

	static
	private double dotProduct(double[] left, double[] right){
		double sum = 0d;

		if(left.length != right.length){
			throw new EvaluationException();
		}

		for(int i = 0; i < left.length; i++){
			sum += (left[i] * right[i]);
		}

		return sum;
	}

	static
	private double squaredDistance(double[] left, double[] right){
		double sum = 0d;

		if(left.length != right.length){
			throw new EvaluationException();
		}

		for(int i = 0; i < left.length; i++){
			double diff = (left[i] - right[i]);

			sum += (diff * diff);
		}

		return sum;
	}
}