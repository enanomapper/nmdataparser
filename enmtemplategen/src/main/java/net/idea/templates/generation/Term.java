package net.idea.templates.generation;

public class Term {

	protected Term secondbest;

	public Term getSecondbest() {
		return secondbest;
	}

	public void setSecondbest(Term secondbest) {
		this.secondbest = secondbest;
	}

	public Term() {
		setFrequency(1);
		setDistance(0);
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	int frequency;

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	String annotation;
	String label;
	double distance;

	@Override
	public String toString() {
		return String.format("%d\t\"%s\"\t\"%s\"\t%s\t%s", frequency,
				annotation, label, distance, secondbest);
	}
}
