package com.coxandkings.bre.model;

import oracle.rules.sdk2.dictionary.DictionaryFinder;
import oracle.rules.sdk2.exception.RepositoryException;
import oracle.rules.sdk2.repository.MDSRepository;

public class RuleDictionary {
	private MDSRepository mdsRepo;
	private String rulesDictLoc; 
	private oracle.rules.sdk2.dictionary.RuleDictionary ruleDict;
	
	RuleDictionary(MDSRepository mdsRepo, String rulesLoc) throws RepositoryException {
		this.mdsRepo = mdsRepo;
		rulesDictLoc = rulesLoc;
		intialize();
	}
	
	private void intialize() throws RepositoryException {
		DictionaryFinder dictFinder = mdsRepo.getFinder(null);
		ruleDict = dictFinder.findDictionary(getRulesDictionaryLocation());
		
	}
	
	public String getRulesDictionaryLocation() {
		return rulesDictLoc;
	}
	
	
}