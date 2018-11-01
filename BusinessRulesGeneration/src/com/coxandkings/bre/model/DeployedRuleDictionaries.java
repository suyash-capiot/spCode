package com.coxandkings.bre.model;

import java.util.ArrayList;
import java.util.List;

import oracle.rules.sdk2.repository.DictionaryExplorer;
import oracle.rules.sdk2.repository.MDSRepository;
import oracle.rules.sdk2.repository.Repositories;

public class DeployedRuleDictionaries {
	
	private ArrayList<RuleDictionary> depRuleDicts;
	
	public RuleDictionary[] getRuleDictionaries() {
		try {
			MDSRepository mdsRepo = Repositories.getRepository();
			
			DictionaryExplorer dictExplorer = mdsRepo.getExplorer(null, null);
			List<String> dictList = dictExplorer.list(null, true);
			for (String dictName : dictList) {
				depRuleDicts.add(new RuleDictionary(mdsRepo, dictName));
			}
		}
		catch (Exception x) {
			x.printStackTrace();
		}

		return depRuleDicts.toArray(new RuleDictionary[depRuleDicts.size()]);
	}

}
