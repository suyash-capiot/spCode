

import java.io.FileReader;
import java.util.List;

import oracle.rules.sdk2.decisionpoint.DecisionPointDictionaryFinder;
import oracle.rules.sdk2.dictionary.RuleDictionary;

public class RulesReaderAndGenerator {
	private static String rulesLocation = "D:/JDeveloper/mywork/BRE_CNK1/BRE_CNK/SOA/oracle/rules/bre_cnk/FareCalculationsBusinessRules.rules";

	public static void main(String[] args) {
		FileReader fr = null;
		
		try {
			fr = new FileReader(rulesLocation);
			RuleDictionary rld = RuleDictionary.readDictionary(fr, new DecisionPointDictionaryFinder());
			RuleDictionary rldh = rld.createHandle();
			
			List<RuleDictionary> rldList = rldh.getDictionaries();
			for (RuleDictionary rldd : rldList) {
				System.out.println(rldd.getName());
			}
		}
		catch (Exception x) {
			x.printStackTrace();
		}
		finally {
			if (fr != null) {
				try { fr.close(); } 
				catch (Exception ex) { }
			}
		}
	}

}
