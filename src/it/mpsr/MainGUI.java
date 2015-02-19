package it.mpsr;

public class MainGUI {

	public static void main(String[] args) {
		
		MeanValueAnalysisTest mva_test = new MeanValueAnalysisTest();
		GordonNewellTest gn_test = new GordonNewellTest();
		
		GeneralFrame gf = new GeneralFrame(mva_test.getMva(), gn_test.getGn());
	}

}
